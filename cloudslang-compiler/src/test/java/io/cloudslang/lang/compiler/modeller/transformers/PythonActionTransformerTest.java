/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.ExternalPythonScriptValidator;
import io.cloudslang.lang.compiler.validator.ExternalPythonScriptValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import junit.framework.Assert;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 * Date: 11/11/2014
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PythonActionTransformerTest.Config.class)
public class PythonActionTransformerTest extends TransformersTestParent {

    @Autowired
    private PythonActionTransformer pythonActionTransformer;
    @Autowired
    private YamlParser yamlParser;
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private Map<String, Serializable> initialPythonActionSimple;
    private Map<String, Serializable> initialPythonActionWithDependencies;
    private Map<String, Serializable> initialPythonActionInvalidKey;
    private Map<String, Serializable> initialExternalPythonAction1;
    private Map<String, Serializable> initialExternalPythonAction2;
    private Map<String, Serializable> initialExternalPythonInvalidAction;
    private Map<String, Serializable> initialExternalPythonInvalidAction2;
    private Map<String, Serializable> expectedPythonActionSimple;

    @Before
    public void init() throws URISyntaxException {
        initialPythonActionSimple = loadPythonActionData("/python_action_simple.sl");
        initialPythonActionWithDependencies = loadPythonActionData("/python_action_with_dependencies.sl");
        initialPythonActionInvalidKey = loadPythonActionData("/corrupted/python_action_invalid_key.sl");
        initialPythonActionSimple = loadPythonActionData("/python_action_simple.sl");
        initialExternalPythonAction1 = loadPythonActionData("/python_external_valid_action1.sl");
        initialExternalPythonAction2 = loadPythonActionData("/python_external_valid_action2.sl");
        initialExternalPythonInvalidAction = loadPythonActionData("/python_external_invalid_action1.sl");
        initialExternalPythonInvalidAction2 = loadPythonActionData("/python_external_invalid_action2.sl");


        expectedPythonActionSimple = new LinkedHashMap<>();
        expectedPythonActionSimple.put(SlangTextualKeys.PYTHON_ACTION_SCRIPT_KEY, "pass");
        expectedPythonActionSimple.put(SlangTextualKeys.PYTHON_ACTION_USE_JYTHON_KEY, true);
    }

    @Test
    public void testTransformSimple() throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Serializable> actualPythonActionSimple =
                transformAndThrowErrorIfExists(pythonActionTransformer, initialPythonActionSimple);
        Assert.assertEquals(expectedPythonActionSimple, actualPythonActionSimple);
    }

    @Test
    public void testTransformWithDependencies() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Following tags are invalid: [dependencies]. " +
                "Please take a look at the supported features per versions link");
        transformAndThrowErrorIfExists(pythonActionTransformer, initialPythonActionWithDependencies);
    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testTransformWithEmptyOneEmptyPart() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(DependencyFormatValidator.INVALID_DEPENDENCY);
        transformAndThrowFirstException(pythonActionTransformer,
                loadPythonActionData("/python_action_with_dependencies_1_empty_part.sl"));
    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testTransformWithEmptyDependencies() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(DependencyFormatValidator.INVALID_DEPENDENCY);
        transformAndThrowFirstException(pythonActionTransformer,
                loadPythonActionData("/python_action_with_dependencies_1_part.sl"));
    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testTransformWithAllEmptyParts() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(DependencyFormatValidator.INVALID_DEPENDENCY);
        transformAndThrowFirstException(pythonActionTransformer,
                loadPythonActionData("/python_action_with_dependencies_2_parts.sl"));
    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testTransformWithOnePart() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(DependencyFormatValidator.INVALID_DEPENDENCY);
        transformAndThrowFirstException(pythonActionTransformer,
                loadPythonActionData("/python_action_with_dependencies_all_empty_parts.sl"));
    }

    @Ignore("Enable when `dependencies` tag will be added")
    @Test
    public void testTransformWithTwoEmptyParts() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(DependencyFormatValidator.INVALID_DEPENDENCY);
        transformAndThrowFirstException(pythonActionTransformer,
                loadPythonActionData("/python_action_with_dependencies_empty.sl"));
    }

    @Test
    public void testTransformInvalidKey() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(AbstractTransformer.INVALID_KEYS_ERROR_MESSAGE_PREFIX);
        exception.expectMessage("invalid_key");
        //noinspection unchecked
        transformAndThrowFirstException(pythonActionTransformer, initialPythonActionInvalidKey);
    }

    @Test
    public void testTransformWithExternalPythonValid1() {
        transformAndThrowErrorIfExists(pythonActionTransformer, initialExternalPythonAction1);
    }

    @Test
    public void testTransformWithExternalPythonValid2() {
        transformAndThrowErrorIfExists(pythonActionTransformer, initialExternalPythonAction2);
    }

    @Test
    public void testTransformWithExternalInvalidPythonValid() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Method {execute} is missing or is invalid");

        transformAndThrowErrorIfExists(pythonActionTransformer, initialExternalPythonInvalidAction);
    }

    @Test
    public void testTransformWithExternalInvalidPythonValid2() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Overload of the execution method is not allowed");

        transformAndThrowErrorIfExists(pythonActionTransformer, initialExternalPythonInvalidAction2);
    }

    private Map<String, Serializable> loadPythonActionData(String filePath) throws URISyntaxException {
        URL resource = getClass().getResource(filePath);
        ParsedSlang file = yamlParser.parse(SlangSource.fromFile(new File(resource.toURI())));
        Map op = file.getOperation();
        @SuppressWarnings("unchecked")
        Map<String, Serializable> returnMap = (Map) op.get(SlangTextualKeys.PYTHON_ACTION_KEY);
        return returnMap;
    }


    private Map<String, Serializable> transformAndThrowErrorIfExists(PythonActionTransformer pythonActionTransformer,
                                                                     Map<String, Serializable> rawData) {
        TransformModellingResult<Map<String, Serializable>> transformModellingResult =
                pythonActionTransformer.transform(rawData);
        BasicTransformModellingResult<Map<String, Serializable>> basicTransformModellingResult =
                (BasicTransformModellingResult<Map<String, Serializable>>) transformModellingResult;
        List<RuntimeException> errors = basicTransformModellingResult.getErrors();
        if (CollectionUtils.isNotEmpty(errors)) {
            throw errors.get(0);
        } else {
            return basicTransformModellingResult.getTransformedData();
        }
    }

    public static class Config {
        @Bean
        @Scope("prototype")
        public Yaml yaml() {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public YamlParser yamlParser() {
            return new YamlParser() {
                @Override
                public Yaml getYaml() {
                    return yaml();
                }
            };
        }

        @Bean
        public ParserExceptionHandler parserExceptionHandler() {
            return new ParserExceptionHandler();
        }

        @Bean
        public PythonActionTransformer pythonActionTransformer() {
            PythonActionTransformer pythonActionTransformer = new PythonActionTransformer();
            pythonActionTransformer.setExternalPythonScriptValidator(externalPythonScriptValidator());
            pythonActionTransformer.setDependencyFormatValidator(dependencyFormatValidator());
            return pythonActionTransformer;
        }

        @Bean
        public DependencyFormatValidator dependencyFormatValidator() {
            return new DependencyFormatValidator();
        }

        @Bean
        public ExternalPythonScriptValidator externalPythonScriptValidator() {
            return new ExternalPythonScriptValidatorImpl();
        }

        @Bean
        public ExecutableValidator executableValidator() {
            return new ExecutableValidatorImpl();
        }

        @Bean
        public SystemPropertyValidator systemPropertyValidator() {
            return new SystemPropertyValidatorImpl();
        }
    }
}
