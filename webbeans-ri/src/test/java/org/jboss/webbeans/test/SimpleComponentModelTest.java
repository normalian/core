package org.jboss.webbeans.test;


import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.ConversationScoped;
import javax.webbeans.Current;
import javax.webbeans.Dependent;
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.bindings.ConversationScopedBinding;
import org.jboss.webbeans.bindings.CurrentBinding;
import org.jboss.webbeans.bindings.DependentBinding;
import org.jboss.webbeans.bindings.NamedBinding;
import org.jboss.webbeans.bindings.RequestScopedBinding;
import org.jboss.webbeans.bindings.StandardBinding;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.annotations.HornedMammalStereotype;
import org.jboss.webbeans.test.annotations.MammalStereotype;
import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;
import org.jboss.webbeans.test.annotations.RiverFishStereotype;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.bindings.AnimalStereotypeBinding;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeBinding;
import org.jboss.webbeans.test.bindings.AsynchronousBinding;
import org.jboss.webbeans.test.bindings.FishStereotypeBinding;
import org.jboss.webbeans.test.bindings.HornedAnimalDeploymentTypeBinding;
import org.jboss.webbeans.test.bindings.HornedMamalStereotypeBinding;
import org.jboss.webbeans.test.bindings.RiverFishStereotypeBinding;
import org.jboss.webbeans.test.bindings.SynchronousBinding;
import org.jboss.webbeans.test.components.Antelope;
import org.jboss.webbeans.test.components.Carp;
import org.jboss.webbeans.test.components.Cat;
import org.jboss.webbeans.test.components.Chair;
import org.jboss.webbeans.test.components.ComponentWithTooManyScopeTypes;
import org.jboss.webbeans.test.components.Cow;
import org.jboss.webbeans.test.components.Goldfish;
import org.jboss.webbeans.test.components.Gorilla;
import org.jboss.webbeans.test.components.Haddock;
import org.jboss.webbeans.test.components.Horse;
import org.jboss.webbeans.test.components.Moose;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.test.components.Pig;
import org.jboss.webbeans.test.components.SeaBass;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.test.components.broken.ComponentWithTooManyDeploymentTypes;
import org.jboss.webbeans.test.components.broken.OuterComponent.InnerComponent;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.jboss.webbeans.util.Reflections;
import org.junit.Before;
import org.junit.Test;

public class SimpleComponentModelTest
{
   
   private ContainerImpl container;
   
   private AnnotatedType emptyAnnotatedItem;
   
   @Before
   public void before()
   {
      emptyAnnotatedItem = new SimpleAnnotatedType(null, new HashMap<Class<? extends Annotation>, Annotation>());
      
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new StandardBinding());
      enabledDeploymentTypes.add(new AnotherDeploymentTypeBinding());
      enabledDeploymentTypes.add(new HornedAnimalDeploymentTypeBinding());
      container = new MockContainerImpl(enabledDeploymentTypes);
      
      initStereotypes(container);
   }
   
   private void initStereotypes(ContainerImpl container)
   {
      container.getStereotypeManager().addStereotype(new StereotypeModel(new SimpleAnnotatedType(AnimalStereotype.class)));
      container.getStereotypeManager().addStereotype(new StereotypeModel(new SimpleAnnotatedType(HornedMammalStereotype.class)));
      container.getStereotypeManager().addStereotype(new StereotypeModel(new SimpleAnnotatedType(MammalStereotype.class)));
      container.getStereotypeManager().addStereotype(new StereotypeModel(new SimpleAnnotatedType(FishStereotype.class)));
      container.getStereotypeManager().addStereotype(new StereotypeModel(new SimpleAnnotatedType(RiverFishStereotype.class)));
      container.getStereotypeManager().addStereotype(new StereotypeModel(new SimpleAnnotatedType(RequestScopedAnimalStereotype.class)));
   }
   
   // **** TESTS FOR DEPLOYMENT TYPE **** //
   
   @Test
   public void testTooManyDeploymentTypes()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<ComponentWithTooManyDeploymentTypes>(new SimpleAnnotatedType(ComponentWithTooManyDeploymentTypes.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testXmlDeploymentTypeOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> xmlDefinedDeploymentTypeAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      xmlDefinedDeploymentTypeAnnotations.put(AnotherDeploymentType.class, new AnotherDeploymentTypeBinding());
      AnnotatedType xmlDefinedDeploymentTypeAnnotatedItem = new SimpleAnnotatedType(ComponentWithTooManyDeploymentTypes.class, xmlDefinedDeploymentTypeAnnotations);
      
      SimpleComponentModel<ComponentWithTooManyDeploymentTypes> component = new SimpleComponentModel<ComponentWithTooManyDeploymentTypes>(new SimpleAnnotatedType(ComponentWithTooManyDeploymentTypes.class), xmlDefinedDeploymentTypeAnnotatedItem, container);
      assert component.getDeploymentType().annotationType().equals(AnotherDeploymentType.class);
   }
   
   @Test
   public void testXmlDefaultDeploymentType()
   {
      AnnotatedType antelopeAnnotatedItem = new SimpleAnnotatedType(Antelope.class, new HashMap<Class<? extends Annotation>, Annotation>());
      SimpleComponentModel<Antelope> antelope = new SimpleComponentModel<Antelope>(emptyAnnotatedItem, antelopeAnnotatedItem, container);
      assert antelope.getDeploymentType().annotationType().equals(Production.class);
   }
   
   @Test
   public void testXmlIgnoresJavaDeploymentType()
   {
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Tuna.class, new HashMap<Class<? extends Annotation>, Annotation>());
      SimpleComponentModel<Tuna> tuna = new SimpleComponentModel<Tuna>(new SimpleAnnotatedType(Tuna.class), annotatedItem, container);
      assert tuna.getDeploymentType().annotationType().equals(Production.class);
   }
   
   @Test
   public void testDeploymentTypePrecedenceSelection()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(HornedMammalStereotype.class, new HornedMamalStereotypeBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Moose.class, annotations);
      
      SimpleComponentModel<Moose> moose = new SimpleComponentModel<Moose>(new SimpleAnnotatedType(Moose.class), annotatedItem, container);
      assert moose.getDeploymentType().annotationType().equals(HornedAnimalDeploymentType.class);
      
   }
   
   // **** TESTS FOR BINDING TYPE **** //
   
   @SuppressWarnings("unchecked")
   @Test
   public void testXmlBindingTypeOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Cat.class, annotations);
      
      SimpleComponentModel<Cat> cat = new SimpleComponentModel<Cat>(new SimpleAnnotatedType(Cat.class), annotatedItem, container);
      assert cat.getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(cat.getBindingTypes(), Asynchronous.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testBindingTypesDeclaredInJava()
   {
      SimpleComponentModel<Cat> cat = new SimpleComponentModel<Cat>(new SimpleAnnotatedType(Cat.class), emptyAnnotatedItem, container);
      assert cat.getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(cat.getBindingTypes(), Synchronous.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testBindingTypesDeclaredInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Antelope.class, annotations);
      
      SimpleComponentModel<Antelope> antelope = new SimpleComponentModel<Antelope>(emptyAnnotatedItem, annotatedItem, container);
      assert Reflections.annotationSetMatches(antelope.getBindingTypes(), Asynchronous.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testDefaultBindingType()
   {
      SimpleComponentModel<Order> order = new SimpleComponentModel<Order>(new SimpleAnnotatedType(Order.class), emptyAnnotatedItem, container);
      assert order.getBindingTypes().size() == 1;
      order.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }
   
   // **** TESTS FOR SCOPES **** //
   
   @Test
   public void testScopeDeclaredInJava()
   {
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), emptyAnnotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testScopeDeclaredInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(RequestScoped.class, new RequestScopedBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Order.class, annotations);
      
      SimpleComponentModel<Order> order = new SimpleComponentModel<Order>(new SimpleAnnotatedType(Order.class), annotatedItem, container);
      assert order.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testScopeDeclaredInXmlOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(ConversationScoped.class, new ConversationScopedBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(ConversationScoped.class);
   }
   
   @Test
   public void testScopeMissingInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedType annotatedItem = new SimpleAnnotatedType(SeaBass.class, annotations);
      
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testNoScopeSpecified()
   {
      SimpleComponentModel<Order> order = new SimpleComponentModel<Order>(new SimpleAnnotatedType(Order.class), emptyAnnotatedItem, container);
      assert order.getScopeType().annotationType().equals(Dependent.class);
   }
   
   @Test
   public void testTooManyScopesSpecified()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<ComponentWithTooManyScopeTypes>(new SimpleAnnotatedType(ComponentWithTooManyScopeTypes.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;  
   }
   
   @Test
   public void testTooManyScopesSpecifiedInXml()
   {
      boolean exception = false;
      try
      {
         Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
         annotations.put(RequestScoped.class, new RequestScopedBinding());
         annotations.put(ConversationScoped.class, new ConversationScopedBinding());
         AnnotatedType antelopeAnnotatedItem = new SimpleAnnotatedType(Antelope.class, annotations);
         new SimpleComponentModel<Antelope>(emptyAnnotatedItem, antelopeAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;  
   }
   
   @Test
   public void testScopeSpecifiedAndStereotyped()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testMutipleIncompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeBinding());
      annotations.put(AnimalStereotype.class, new AnimalStereotypeBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Haddock.class, annotations);
      
      boolean exception = false;
      try
      {
         new SimpleComponentModel<Haddock>(new SimpleAnnotatedType(Haddock.class), annotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testMutipleIncompatibleScopeStereotypesWithScopeSpecified()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeBinding());
      annotations.put(AnimalStereotype.class, new AnimalStereotypeBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(SeaBass.class, annotations);
      
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), annotatedItem, container);
      assert trout.getScopeType().annotationType().equals(RequestScoped.class);     
   }
   
   @Test
   public void testMutipleCompatibleScopeStereotypes()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeBinding());
      annotations.put(RiverFishStereotype.class, new RiverFishStereotypeBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Haddock.class, annotations);
      
      SimpleComponentModel<Haddock> haddock = new SimpleComponentModel<Haddock>(new SimpleAnnotatedType(Haddock.class), annotatedItem, container);
      assert haddock.getScopeType().annotationType().equals(ApplicationScoped.class);
   }
   
   // **** TESTS FOR COMPONENT NAME **** /
   
   @Test
   public void testNamed()
   {
      SimpleComponentModel<Haddock> haddock = new SimpleComponentModel<Haddock>(new SimpleAnnotatedType(Haddock.class), emptyAnnotatedItem, container);
      assert haddock.getName() != null;
      assert haddock.getName().equals("haddock");
   }
   
   @Test
   public void testXmlNamed()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedBinding()
      {

         public String value()
         {
            return "";
         }
      });
      AnnotatedType annotatedItem = new SimpleAnnotatedType(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), annotatedItem, container);
      
      assert trout.getName() != null;
      assert trout.getName().equals("seaBass");
   }
   
   @Test
   public void testNonDefaultXmlNamed()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedBinding()
      {

         public String value()
         {
            return "aTrout";
         }
      });
      AnnotatedType annotatedItem = new SimpleAnnotatedType(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), annotatedItem, container);
      
      assert trout.getName().equals("aTrout");
   }
   
   @Test
   public void testNotNamed()
   {
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType(SeaBass.class), emptyAnnotatedItem, container);
      assert trout.getName() == null;
   }
   
   @Test
   public void testNonDefaultNamed()
   {
      SimpleComponentModel<Moose> moose = new SimpleComponentModel<Moose>(new SimpleAnnotatedType(Moose.class), emptyAnnotatedItem, container);
      assert moose.getName().equals("aMoose");
   }
   
   
   // **** TESTS FOR STEREOTYPES **** //
   
   @SuppressWarnings("unchecked")
   @Test
   public void testStereotypeDeclaredInXmlAndJava()
   {
      Map<Class<? extends Annotation>, Annotation> orderXmlAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      orderXmlAnnotations.put(Current.class, new CurrentBinding());
      orderXmlAnnotations.put(Synchronous.class, new SynchronousBinding());
      orderXmlAnnotations.put(Named.class, new NamedBinding()
      {
         public String value()
         {
            return "currentSynchronousOrder";
         }
      });
      AnnotatedType currentSynchronousOrderAnnotatedItem = new SimpleAnnotatedType(Order.class, orderXmlAnnotations);
      
      SimpleComponentModel<Order> order = new SimpleComponentModel<Order>(new SimpleAnnotatedType(Order.class), currentSynchronousOrderAnnotatedItem, container);
      assert Production.class.equals(order.getDeploymentType().annotationType());
      assert "currentSynchronousOrder".equals(order.getName());
      assert order.getBindingTypes().size() == 2;
      assert Reflections.annotationSetMatches(order.getBindingTypes(), Current.class, Synchronous.class);
      assert order.getScopeType().annotationType().equals(Dependent.class);
   }
   
   @Test
   public void testSingleStereotype()
   {
      SimpleComponentModel<Gorilla> gorilla = new SimpleComponentModel<Gorilla>(new SimpleAnnotatedType(Gorilla.class), emptyAnnotatedItem, container);
      assert gorilla.getName() == null;
      assert gorilla.getDeploymentType().annotationType().equals(Production.class);
      assert gorilla.getBindingTypes().iterator().next().annotationType().equals(Current.class);
      assert gorilla.getScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test
   public void testRequiredTypeIsImplemented()
   {
      try
      {
         new SimpleComponentModel<Gorilla>(new SimpleAnnotatedType(Gorilla.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         assert false;
      }
      
   }
   
   @Test
   public void testRequiredTypeIsNotImplemented()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<Chair>(new SimpleAnnotatedType(Chair.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
   @Test
   public void testScopeIsSupported()
   {
      try
      {
         new SimpleComponentModel<Goldfish>(new SimpleAnnotatedType(Goldfish.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         e.printStackTrace();
         assert false;
      }
      
   }
   
   @Test
   public void testScopeIsNotSupported()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<Carp>(new SimpleAnnotatedType(Carp.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
   //*** COMPONENT CLASS CHECKS ****//
   
   @Test
   public void testAbstractClassIsNotAllowed()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<Cow>(new SimpleAnnotatedType(Cow.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testInnerClassIsNotAllowed()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<InnerComponent>(new SimpleAnnotatedType(InnerComponent.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testFinalClassMustBeDependentScoped()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<Horse>(new SimpleAnnotatedType(Horse.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Dependent.class, new DependentBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Horse.class, annotations);
      try
      {
         new SimpleComponentModel<Horse>(new SimpleAnnotatedType(Horse.class), annotatedItem, container);
      }
      catch (Exception e) 
      {
         assert false;
      }
   }
   
   @Test
   public void testClassWithFinalMethodMustBeDependentScoped()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<Pig>(new SimpleAnnotatedType(Pig.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Dependent.class, new DependentBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Pig.class, annotations);
      try
      {
         new SimpleComponentModel<Pig>(new SimpleAnnotatedType(Pig.class), annotatedItem, container);
      }
      catch (Exception e) 
      {
         assert false;
      }
   }
   
}
