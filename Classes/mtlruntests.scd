(
var report = true;
var verbose= UnitTest.brief; // or UnitTest.full

UnitTest.passVerbosity = verbose;
MtlScalaParserTests.run(report:report);
MtlMitolaParserTests.run(report:report);
MtlRootFrequencyCalculatorTests.run(report:report);
MtlMitolaTests.run(report:report);
MtlDegreeMapperTests.run(report:report);
MtlDegreeTests.run(report:report);
MtlScalaCalculatorTests.run(report:report);
)