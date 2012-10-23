package org.bigraph.model.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	BigraphTests.class,
	SignatureTests.class,
	ExecutionTests.class
})
public class ModelTests {
}
