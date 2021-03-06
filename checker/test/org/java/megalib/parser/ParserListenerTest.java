package org.java.megalib.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.java.megalib.checker.services.ModelLoader;
import org.java.megalib.models.Function;
import org.java.megalib.models.Relation;
import org.junit.Test;

/**
 * Tests the correct behavior for MegaModelLoader and MegaModel functionality.
 * Especially the constraints relevant for creation time are tested.
 *
 * @author heinz
 */
public class ParserListenerTest {

    @Test
    public void preludeIsParsed() {
        ModelLoader ml = new ModelLoader();
        assertEquals(0, ml.getTypeErrors().size());
        assertEquals(6, ml.getModel().getBlocks().size());
        ml.getModel().getBlocks().forEach(b -> assertTrue(b.getText().startsWith("/*") && b.getText().endsWith("*/"))); //TODO
    }

    @Test
    public void addSubtypeInvalidSupertype() throws ParserException, IOException {
        String input = "/**/DerivedType < Type.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, String> subtypes = ml.getModel().getSubtypesMap();

        assertFalse(subtypes.containsKey("DerivedType"));
        
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at DerivedType: The declared supertype is not a subtype of Entity",ml.getTypeErrors().get(0));
    }

    @Test
    public void addSubtypeEntity() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/Type < Entity. " + "Entity < Type.";
        ml.loadString(input);
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at Entity < Type: Entity is a MegaL keyword.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addSubtypeValidSupertype() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/DerivedType < Artifact.";
        ml.loadString(input);
        Map<String, String> subtypes = ml.getModel().getSubtypesMap();

        assertTrue(subtypes.containsKey("DerivedType"));
        assertEquals(0, ml.getTypeErrors().size());
        assertEquals("Artifact", subtypes.get("DerivedType"));
    }

    @Test
    public void addSubtypeOfEntity() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/ DerivedType < Entity; = \"http://www.softlang.org/\".";
        ml.loadString(input);
        Map<String, String> subtypes = ml.getModel().getSubtypesMap();

        assertTrue(subtypes.containsKey("DerivedType"));
        assertEquals(0, ml.getTypeErrors().size());
        assertEquals("Entity", subtypes.get("DerivedType"));
    }

    @Test
    public void addSubtypeMultipleInh() throws ParserException, IOException {
        String input = "/**/DerivedType < Technology. DerivedType < System.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, String> subtypes = ml.getModel().getSubtypesMap();

        assertTrue(subtypes.containsKey("DerivedType"));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at DerivedType: Multiple inheritance is not allowed.",ml.getTypeErrors().get(0));
        assertEquals("Technology", subtypes.get("DerivedType"));
    }

    @Test
    public void addEntityInstance() throws ParserException, IOException {
        String input = "/**/Entity : Artifact.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, String> imap = ml.getModel().getInstanceOfMap();
        assertFalse(imap.containsKey("Entity"));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at Entity: The root type `Entity' cannot be instantiated.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addInstanceOfUnknown() throws ParserException, IOException {
        String input = "/**/Instance : Type.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, String> imap = ml.getModel().getInstanceOfMap();

        assertFalse(imap.containsKey("Instance"));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at Instance: The instantiated type is not (transitive) subtype of Entity.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addInstanceOfEntity() throws ParserException, IOException {
        String input = "/**/Artifact : Technology.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, String> imap = ml.getModel().getInstanceOfMap();

        assertFalse(imap.containsKey("Artifact"));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at Artifact: It is instance and type at the same time.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addInstanceOfMulitple() throws ParserException, IOException {
        String input = "/**/t : Technology. " + "t : Artifact.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, String> imap = ml.getModel().getInstanceOfMap();

        assertEquals("Technology", imap.get("t"));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at t: Multiple types cannot be assigned to the same instance.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addInstanceOfProgrammingLanguage() throws ParserException, IOException {
        String input = "/**/XYZ : ProgrammingLanguage.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, String> imap = ml.getModel().getInstanceOfMap();
        assertEquals("ProgrammingLanguage", imap.get("Java"));
        assertEquals(0, ml.getTypeErrors().size());
    }

    @Test
    public void addInstanceOfArtifact() throws ParserException, IOException {
        String input = "/**/a : File; " + "elementOf Python; "
                + "hasRole MvcModel.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
        Map<String, String> imap = ml.getModel().getInstanceOfMap();
        assertTrue(imap.containsKey("a"));
        assertEquals("File", imap.get("a"));
        assertTrue(ml.getModel().getRelationships().get("elementOf").contains(new Relation("a", "Python")));
        assertTrue(ml.getModel().getRelationships().get("hasRole").contains(new Relation("a", "MvcModel")));
    }

    @Test
    public void addInversePartOf() throws ParserException, IOException {
        String input = "/**/ANTLRXYZ : ProgrammingLanguage. XYZ : ProgrammingLanguage; ^subsetOf ANTLRXYZ.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
        Map<String,String> imap = ml.getModel().getInstanceOfMap();
        assertTrue(imap.containsKey("ANTLRXYZ"));
        assertTrue(imap.containsKey("XYZ"));
        assertTrue(ml.getModel().getRelationships().containsKey("subsetOf"));
        Set<Relation> rmap = ml.getModel().getRelationships().get("subsetOf");
        assertTrue(rmap.contains(new Relation("ANTLRXYZ", "XYZ")));
    }

    @Test
    public void addRelationDeclarationUnknownDomain() throws ParserException, IOException {
        String input = "/**/Relation < TypeOne # Artifact.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Relation>> actual = ml.getModel().getRelationshipDeclarationMap();

        assertFalse(actual.containsKey("Relation"));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at declaration of Relation: Its domain TypeOne is not subtype of Entity.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addRelationDeclarationUnknownRange() throws ParserException, IOException {
        String input = "/**/Relation < Artifact # TypeTwo.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Relation>> actual = ml.getModel().getRelationshipDeclarationMap();

        assertFalse(actual.containsKey("Relation"));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at declaration of Relation: Its range TypeTwo is not subtype of Entity.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addRelationDeclarationDouble() throws ParserException, IOException {
        String input = "/**/Relation < Artifact # Artifact. " + "Relation < Artifact # Artifact.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Relation>> actual = ml.getModel().getRelationshipDeclarationMap();

        assertTrue(actual.containsKey("Relation"));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at declaration of Relation: It is declared twice with the same types.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addRelationDeclarationOverloaded() throws ParserException, IOException {
        String input = "/**/Relation < Artifact # Artifact. Relation < Technology # Technology.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Relation>> actual = ml.getModel().getRelationshipDeclarationMap();

        assertTrue(actual.containsKey("Relation"));
        assertEquals(0, ml.getTypeErrors().size());
        assertTrue(actual.get("Relation").contains(new Relation("Artifact", "Artifact")));
        assertTrue(actual.get("Relation").contains(new Relation("Technology", "Technology")));
    }

    @Test
    public void addRelationInstanceUndeclared() throws ParserException, IOException {
        String input = "/**/a : ProgrammingLanguage. " + "b : ProgrammingLanguage; " + "r b.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at instance of r: r is not declared.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addRelationInstanceDouble() throws ParserException, IOException {
        String input = "/**/a : ProgrammingLanguage. " + "b : ProgrammingLanguage. a subsetOf b. a subsetOf b.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Relation>> actual = ml.getModel().getRelationships();

        assertTrue(actual.containsKey("subsetOf"));
        assertTrue(actual.get("subsetOf").contains(new Relation("a", "b")));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error: 'a subsetOf b' already exists.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addRelationInstanceUnfitDomain() throws ParserException, IOException {
        String input = "/**/a : Framework. b : ProgrammingLanguage. a subsetOf b.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Relation>> actual = ml.getModel().getRelationships();
        Relation r = new Relation("a", "b");
        assertFalse(actual.get("subsetOf").contains(r));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at instance of subsetOf: 'a subsetOf b' does not fit any declaration.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addRelationInstanceUnfitRange() throws ParserException, IOException {
        String input = "/**/a : Framework. b : ProgrammingLanguage. b subsetOf a.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Relation>> actual = ml.getModel().getRelationships();
        Relation r = new Relation("a", "b");
        assertFalse(actual.get("subsetOf").contains(r));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at instance of subsetOf: 'b subsetOf a' does not fit any declaration.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addRelationInstanceMultiFit() throws ParserException, IOException {
        String input = "/**/subsetOf < ProgrammingLanguage # ProgrammingLanguage. a : ProgrammingLanguage. "
                + "b : ProgrammingLanguage. " + "a subsetOf b. ";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Relation>> actual = ml.getModel().getRelationships();

        Relation r = new Relation("a", "b");
        assertFalse(actual.get("subsetOf").contains(r));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at instance of subsetOf: 'a subsetOf b' fits multiple declarations.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addRelationInstanceDomainNotInstance() throws ParserException, IOException {
        String input = "/**/b : ProgrammingLanguage. " + "a subsetOf b.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Relation>> actual = ml.getModel().getRelationships();
        Relation r = new Relation("a", "b");
        assertFalse(actual.get("subsetOf").contains(r));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at relationship 'subsetOf': a is not instantiated.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addRelationInstanceRangeNotInstance() throws ParserException, IOException {
        String input = "/**/a : ProgrammingLanguage. " + "a subsetOf b.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Relation>> actual = ml.getModel().getRelationships();
        Relation r = new Relation("a", "b");
        assertFalse(actual.get("subsetOf").contains(r));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at relationship 'subsetOf': b is not instantiated.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionDeclarationOverloading() throws ParserException, IOException {
        String input = "/**/a : ProgrammingLanguage. " + "b : ProgrammingLanguage. " + "f : a -> a. " + "f : b -> b.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String,Set<Function>> actual = ml.getModel().getFunctionDeclarations();
        assertTrue(actual.containsKey("f"));

        assertEquals(0, ml.getTypeErrors().size());
    }

    @Test
    public void addFunctionDeclarationDomainNotInstantiated() throws ParserException, IOException {
        String input = "/**/b : ProgrammingLanguage. " + "f : a -> b. ";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String,Set<Function>> actual = ml.getModel().getFunctionDeclarations();
        assertFalse(actual.containsKey("f"));

        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at f's declaration: The domain a was not declared.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionDeclarationRangeNotInstantiated() throws ParserException, IOException {
        String input = "/**/a : ProgrammingLanguage. " + "f : a -> b. ";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String,Set<Function>> actual = ml.getModel().getFunctionDeclarations();
        assertFalse(actual.containsKey("f"));

        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at f's declaration: The range b was not declared.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionDeclarationDomainMultipleNotInstantiated() throws ParserException, IOException {
        String input = "/**/b : ProgrammingLanguage. " + "f : b # a # b -> b # b # a. ";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String,Set<Function>> actual = ml.getModel().getFunctionDeclarations();
        assertFalse(actual.containsKey("f"));
        assertEquals(2, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at f's declaration: The domain a was not declared.",ml.getTypeErrors().get(0));
        assertEquals("Testblock0>> Error at f's declaration: The range a was not declared.",ml.getTypeErrors().get(1));
    }

    @Test
    public void addFunctionDeclarationRangeMultipleNotInstantiated() throws ParserException, IOException {
        String input = "/**/b : ProgrammingLanguage. " + "f : b  # b -> b # b # a. ";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String,Set<Function>> actual = ml.getModel().getFunctionDeclarations();
        assertFalse(actual.containsKey("f"));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at f's declaration: The range a was not declared.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionDeclarationDomainNotALanguage() throws ParserException, IOException {
        String input = "/**/b : ProgrammingLanguage. " + "f : Grammar -> b. ";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String,Set<Function>> actual = ml.getModel().getFunctionDeclarations();
        assertFalse(actual.containsKey("f"));

        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at f's declaration: Grammar is neither language nor subtype of Artifact.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionDeclarationRangeNotALanguage() throws ParserException, IOException {
        String input = "/**/b : ProgrammingLanguage. " + "f : b -> Grammar. ";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String,Set<Function>> actual = ml.getModel().getFunctionDeclarations();
        assertFalse(actual.containsKey("f"));
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at f's declaration: Grammar is neither language nor subtype of Artifact.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionDeclaration() throws ParserException, IOException {
        String input = "/**/l : ProgrammingLanguage. " + "f : l -> l.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String,Set<Function>> actual = ml.getModel().getFunctionDeclarations();
        assertEquals(0, ml.getTypeErrors().size());
        assertTrue(actual.containsKey("f"));
    }

    @Test
    public void addFunctionDeclarationMulti() throws ParserException, IOException {
        String input = "/**/a : ProgrammingLanguage. " + "b : ProgrammingLanguage. " + "f : b  # b -> b # b # a. ";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String,Set<Function>> actual = ml.getModel().getFunctionDeclarations();
        assertTrue(actual.containsKey("f"));

        assertEquals(0, ml.getTypeErrors().size());
    }
    
    @Test
    public void addFunctionDeclWithFile() throws ParserException, IOException{
    		String input = "/**/a : ProgrammingLanguage. f : a # File -> File.";
    		ModelLoader ml = new ModelLoader();
    		ml.loadString(input);
    		assertEquals(0,ml.getTypeErrors().size());
    }

    @Test
    public void addFunctionApplicationNotDeclared() throws ParserException, IOException {
        String input = "/**/f(a)|->a.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Function>> actual = ml.getModel().getFunctionApplications();

        assertTrue(actual.isEmpty());
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at application of f: A declaration has to be stated beforehand.",ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionApplication() throws ParserException, IOException {
        String input = "/**/l : ProgrammingLanguage. " + "f : l # l # l -> l # l. " + "a : Artifact. "
                + "a elementOf l. " + "b : Artifact. " + "b elementOf l. " + "f(a,b,a)|->(b,a).";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Function>> actual = ml.getModel().getFunctionApplications();
        assertEquals(1, actual.size());
        assertTrue(actual.containsKey("f"));
        assertEquals(1, actual.get("f").size());
        Function f = actual.get("f").iterator().next();
        assertEquals(3, f.getInputs().size());
        assertEquals("a", f.getInputs().get(0));
        assertEquals("b", f.getInputs().get(1));
        assertEquals("a", f.getInputs().get(2));
        assertEquals(2, f.getOutputs().size());
        assertEquals("b", f.getOutputs().get(0));
        assertEquals("a", f.getOutputs().get(1));
    }

    @Test
    public void addFunctionApplicationDuplicate() throws ParserException, IOException {
        String input = "/**/l : ProgrammingLanguage. " + "f : l # l # l -> l # l. " + "a : Artifact. "
                       + "a elementOf l. " + "b : Artifact. " + "b elementOf l. " + "d : Artifact. " + "d elementOf l. "
                       + "f(a,b,d)|->(b,a). " + "f(a,b,d)|->(b,a).";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Function>> actual = ml.getModel().getFunctionApplications();
        assertTrue(actual.containsKey("f"));
        assertEquals(1, actual.get("f").size());
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at application of f with inputs [a, b, d] and outputs [b, a]: It already exists.",ml.getTypeErrors().get(0));

    }

    @Test
    public void addFunctionApplicationNotInstantiatedInput() throws ParserException, IOException {
        String input = "/**/l : DataExchangeLanguage. " + "f : l -> l. " + "b : Artifact. " + "b elementOf l. "
                + "f(a)|->b.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Function>> actual = ml.getModel().getFunctionApplications();
        assertTrue(actual.isEmpty());
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at application of f: a is not instance of Artifact.", ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionApplicationNotInstantiatedOutput() throws ParserException, IOException {
        String input = "/**/l : DataExchangeLanguage. " + "f : l -> l. " + "a : Artifact. " + "a elementOf l. "
                + "f(a)|->b.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Function>> actual = ml.getModel().getFunctionApplications();
        assertTrue(actual.isEmpty());
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at application of f: b is not an instance of Artifact.", ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionApplicationUnfitDomain() throws ParserException, IOException {
        String input = "/**/l1 : DataExchangeLanguage. " + "l2 : DataExchangeLanguage. "
                + "f : l1 # l2 -> l2. " + "a1 : File. " + "a1 elementOf l1. " + "a1 hasRole MvcModel. "
                + "a2 : Artifact. " + "a2 elementOf l2. " + "f(a1,a1)|->a2.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Function>> actual = ml.getModel().getFunctionApplications();
        assertTrue(actual.isEmpty());
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at application of f with inputs [a1, a1] and outputs [a2]: It does not fit any declaration",
                     ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionApplicationUnfitRange() throws ParserException, IOException {
        String input = "/**/l1 : DataExchangeLanguage. " + "l2 : DataExchangeLanguage. "
                + "f : l1 # l2 -> l2. " + "a1 : Artifact. " + "a1 elementOf l1. " + "a2 : Artifact. "
                + "a2 elementOf l2. " + "f(a1,a2)|->a1.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Function>> actual = ml.getModel().getFunctionApplications();
        assertTrue(actual.isEmpty());
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at application of f with inputs [a1, a2] and outputs [a1]: It does not fit any declaration",
                     ml.getTypeErrors().get(0));
    }

    @Test
    public void addFunctionApplicationSubset() throws ParserException, IOException {
        String input = "/**/l1 : DataExchangeLanguage. " + "l2 : DataExchangeLanguage. "
                + "l2 subsetOf l1. " + "f : l1 # l2 -> l2. " + "a1 : Artifact. " + "a1 elementOf l1. "
                + "a2 : Artifact. " + "a2 elementOf l2. " + "f(a2,a2)|->a2.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        Map<String, Set<Function>> actual = ml.getModel().getFunctionApplications();
        assertTrue(actual.containsKey("f"));
        assertEquals(0, ml.getTypeErrors().size());
    }
    
    @Test
    public void addFunctionApplicationOfFile() throws ParserException, IOException {
    		String input = "/**/ a : ProgrammingLanguage . f : a # File -> File. "
    				+ "b : ProgrammingLanguage. f1 : File; elementOf b. f2 : File; elementOf a. "
    				+ "f3 : File; elementOf b. f(f2,f1)|->f3.";
    		ModelLoader ml = new ModelLoader();
    		ml.loadString(input);
    		assertEquals(0,ml.getTypeErrors().size());
    }

    @Test(expected = FileNotFoundException.class)
    public void fileNotFoundReturnsCriticalError() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        ml.loadFile("");
    }

    @Test(expected = ParserException.class)
    public void testSyntacticallyInvalidString() throws ParserException, IOException {
        String input = "xy test";
        new ModelLoader().loadString(input);
    }

    @Test
    public void testTurtleSyntaxStepwiseFailure() throws ParserException, IOException {
        String input = "/**/a : Artifact; " + "elementOf XYZ;" + "hasRole MvcModel.";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        assertEquals(1, ml.getTypeErrors().size());
        assertEquals("Testblock0>> Error at relationship 'elementOf': XYZ is not instantiated.", ml.getTypeErrors().get(0));
    }

    @Test
    public void testTurtleInstanceLink() throws ParserException, IOException {
        String input = "/**/XYZ : ProgrammingLanguage;"
                + "    = \"https://en.wikipedia.org/wiki/Java_(programming_language)\".";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
    }

    @Test
    public void testTurtleTypeLink() throws ParserException, IOException {
        String input = "/**/Type < Entity;\n" + "=\"https://en.wikipedia.org/wiki/Computer_language\".";
        ModelLoader ml = new ModelLoader();
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
    }

    @Test
    public void testCommentAfterStmt() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/a : ProgrammingLanguage. // test hello world";

        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
    }

    @Test
    public void checkValidFilePath() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/a : Artifact;" + "~= \"file://checker.jar\".";
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
    }

    @Test
    public void checkNameSpaceURL() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/sl :: \"http://softlang.org\". a : Artifact;" + "~= \"sl::course:techmodels\".";
        ml.loadString(input);
        assertEquals("http://softlang.org", ml.getModel().getNamespace("sl"));
        assertEquals(0, ml.getTypeErrors().size());
    }

    @Test
    public void checkNameSpaceFilepath() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/models :: \"file://../models\". a : Artifact;"
                       + "~= \"models::common/Prelude.megal\".";
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
    }
    
    @Test
    public void checkEmpty() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "";
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
    }
}
