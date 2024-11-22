package ppl.momofin.momofinbackend;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@SuppressWarnings("NewClassNamingConvention")
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/resources/features"},
        glue = {"ppl.momofin.momofinbackend.steps"},
        plugin = {"pretty", "json:target/cucumber-report.json"},
        monochrome = true
)
public class CucumberTestRunner {
}
