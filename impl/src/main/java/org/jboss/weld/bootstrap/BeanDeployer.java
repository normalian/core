/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.bootstrap;

import java.util.HashSet;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.interceptor.Interceptor;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.resources.ClassTransformer;

/**
 * @author pmuir
 *
 */
public class BeanDeployer extends AbstractBeanDeployer<BeanDeployerEnvironment>
{
   
   private final BeanManagerImpl deploymentManager;

   private final Set<WeldClass<?>> classes;

   /**
    * @param manager
    * @param ejbDescriptors
    */
   public BeanDeployer(BeanManagerImpl manager, BeanManagerImpl deploymentManager, EjbDescriptors ejbDescriptors)
   {
      super(manager, new BeanDeployerEnvironment(ejbDescriptors, manager));
      this.classes = new HashSet<WeldClass<?>>();
      this.deploymentManager = deploymentManager;
   }

   public BeanDeployer addClass(Class<?> clazz)
   {
      ClassTransformer classTransformer = Container.instance().deploymentServices().get(ClassTransformer.class);
      if (!clazz.isAnnotation() && !clazz.isEnum())
      {
         ProcessAnnotatedTypeImpl<?> event = fireProcessAnnotatedTypeEvent(classTransformer.loadClass(clazz));
         if (!event.isVeto())
         {
            if (event.getAnnotatedType() instanceof WeldClass<?>)
            {
               classes.add((WeldClass<?>) event.getAnnotatedType());
            }
            else
            {
               classes.add(classTransformer.loadClass(event.getAnnotatedType()));
            }
         }
      }
      return this;
   }
   
   // TODO Do we need to fire PAT for annotated types added via BBD? Probably not PLM.
   public BeanDeployer addClass(AnnotatedType<?> clazz)
   {
      ClassTransformer classTransformer = Container.instance().deploymentServices().get(ClassTransformer.class);
      classes.add(classTransformer.loadClass(clazz));
      return this;
   }

   public BeanDeployer addClasses(Iterable<Class<?>> classes)
   {
      for (Class<?> clazz : classes)
      {
         addClass(clazz);
      }
      return this;
   }

   public BeanDeployer createBeans()
   {
      for (WeldClass<?> clazz : classes)
      {
         boolean managedBeanOrDecorator = !getEnvironment().getEjbDescriptors().contains(clazz.getJavaClass()) && isTypeManagedBeanOrDecorator(clazz);
         if (managedBeanOrDecorator && clazz.isAnnotationPresent(Decorator.class))
         {
            createDecorator(clazz);
         }
         else if (managedBeanOrDecorator && clazz.isAnnotationPresent(Interceptor.class))
         {
            createInterceptor(clazz);
         }
         else if (managedBeanOrDecorator && !clazz.isAbstract())
         {
            createSimpleBean(clazz);
         }
      }
      for (InternalEjbDescriptor<?> ejbDescriptor : getEnvironment().getEjbDescriptors())
      {
         createEnterpriseBean(ejbDescriptor);
      }
      return this;
   }

}
