import android.util.Log
import com.instructure.dataseeding.api.SeedApi
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.junit.CucumberOptions

@CucumberOptions(features = ["src/androidTest/java/com/instructure/teacher/ui/cucumber/features/Dashboard.feature"],
    glue = ["com.instructure.teacher.ui.cucumber.steps"])
class LoginStepDefinitions : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    private var data: SeedApi.SeededDataApiModel? = null

    @Given("^Seed test data.$")
    fun iAmOnLoginPage() : SeedApi.SeededDataApiModel {
        if(data == null) {
            Log.d(PREPARATION_TAG, "Seeding data.")
            data = seedData(teachers = 1, courses = 2)

        }
        return data!!
    }

    @When("^I login with my only test user.$")
    fun loginWithUser() {
        val teacher = data!!.teachersList[0]
        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
    }

    @Then("^I should be redirected to the dashboard page$")
    fun verifyDashboardPage() {
        val course1 = data!!.coursesList[0]
        val course2 = data!!.coursesList[1]
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG,"Assert that the '${course1.name}' and '${course2.name}' courses are displayed.")
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertDisplaysCourse(course2)
    }

    @When("^I enter my valid username \"([^\"]*)\" and password \"([^\"]*)\"$")
    fun enterValidCredentials(username: String, password: String) {
        // Add code here to enter the valid username and password in the login fields
    }

    @When("^I click the login button$")
    fun clickLoginButton() {
        // Add code here to click the login button
    }



    @Then("^I should see an error message \"([^\"]*)\"$")
    fun verifyErrorMessage(errorMessage: String) {
        // Add code here to verify that the displayed error message matches the expected one
    }

}
