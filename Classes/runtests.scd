(
var report = true;
var verbose= UnitTest.brief; // or UnitTest.full

UnitTest.passVerbosity = verbose;
ScalaParserTests.run(report:report);
MitolaParserTests.run(report:report);
ScalaCalculatorTests.run(report:report);
RootFrequencyCalculatorTests.run(report:report);
MitolaTests.run(report:report);
)