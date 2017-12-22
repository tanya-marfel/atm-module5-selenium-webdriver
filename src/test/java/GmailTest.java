import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.readAllBytes;

public class GmailTest {
    private WebDriver driver;

    @FindBy(xpath = "//input[@id=\"identifierId\"]")
    private WebElement USERNAMEFIELD;

    @FindBy(xpath = "//input[@name=\"password\"]")
    private WebElement PASSWORDFIELD;

    @FindBy(xpath = "//div[contains(text(), \"COMPOSE\")]")
    private WebElement COMPOSEBUTTON;

    @FindBy(css = "div[role=dialog]")
    private WebElement LETTERWINDOW;

    @FindBy(name = "subjectbox")
    private WebElement SUBJECTFIELD;

    @FindBy(xpath = "//textarea[@name=\"to\"]")
    private WebElement ADDRESSEEFIELD;

    @FindBy(xpath = "//div[@role=\"textbox\"]")
    private WebElement LETTERBODY;

    @FindBy(xpath = "//img[@alt=\"Close\"]")
    private WebElement CLOSEBUTTON;

    @FindBy(partialLinkText = "Drafts")
    private WebElement DRAFTSFOLDER;

    @FindBy(xpath = "//span[contains(text(), 'LoremIpsum')]")
    private WebElement SAVEDDRAFT;

    @FindBy(xpath = "//input[@name=\"to\"]")
    private WebElement SAVEDADDRESSEE;

    @FindBy(xpath = "//input[@name=\"subject\"]")
    private WebElement SAVEDSUBJECT;

    @FindBy(xpath = "//div[contains(text(), \"Send\")]")
    private WebElement SENDBUTTON;

    @FindBy(xpath = "//div[contains(text(), \"Your message has been sent\")]")
    private WebElement SUCCESSMESSAGE;

    @FindBy(css = "a[title=\"Sent Mail\"]")
    private WebElement SENTFOLDER;

    @FindBy(xpath = "//a[contains(@title, \"Google Account\")]")
    private WebElement ACCOUNTBUTTON;

    @FindBy(xpath = "//a[contains(text(),\"Sign out\")]")
    private WebElement SIGNOUTBUTTON;


    @BeforeClass(description = "Launch browser")
    public void launchBrowser() {
        System.setProperty("webdriver.chrome.driver", "./src/main/resources/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        driver = new ChromeDriver();
        PageFactory.initElements(driver, this);
    }

    @BeforeClass(dependsOnMethods = "launchBrowser", description = "Add implicit wait and maximize window")
    public void addImplicitWait() {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
    }

    @Parameters({"URL", "USERNAME", "PASSWORD"})
    @Test(description = "Sign in to Gmail account")
    public void loginToGmail(String URL, String USERNAME, String PASSWORD) {
        driver.get(URL);
        USERNAMEFIELD.click();
        USERNAMEFIELD.sendKeys(USERNAME + Keys.ENTER);
        Assert.assertEquals(USERNAMEFIELD.getAttribute("data-initial-value"), USERNAME);
        waitForVisibilityExplicitly(PASSWORDFIELD);
        PASSWORDFIELD.click();
        PASSWORDFIELD.sendKeys(PASSWORD + Keys.ENTER);

    }

    @Test(description = "Assert, that the login is successful", dependsOnMethods = "loginToGmail")
    public void confirmLoginSuccess() {
        waitForVisibilityExplicitly(COMPOSEBUTTON);
        Assert.assertTrue(isDisplayed(COMPOSEBUTTON));
    }

    @Parameters({"ADDRESSEE", "SUBJECT", "FILENAME"})
    @Test(description = "Create a new mail (fill addressee, subject and body fields)", dependsOnMethods = "confirmLoginSuccess")
    public void composeMail(String ADDRESSEE, String SUBJECT, String FILENAME) {
        COMPOSEBUTTON.click();

        Assert.assertTrue(isDisplayed(LETTERWINDOW));

        ADDRESSEEFIELD.click();
        ADDRESSEEFIELD.sendKeys(ADDRESSEE + Keys.ENTER);

        SUBJECTFIELD.click();
        SUBJECTFIELD.sendKeys(SUBJECT + Keys.TAB);

        LETTERBODY.click();

        try {
            LETTERBODY.sendKeys(readFile(FILENAME));
        } catch (IOException e) {
            System.out.println("There was an error while reading the file caused by " + e.getMessage());
        }
    }

    @Test(description = "Closing the written letter (as an alternative to saving draft)", dependsOnMethods = "composeMail")
    public void closeDraft() {
        CLOSEBUTTON.click();
        waitForInvisibilityFluently(LETTERWINDOW);
        Assert.assertFalse(isDisplayed(LETTERWINDOW));
    }

    @Test(description = "Verify, that the mail presents in ‘Drafts’ folder", dependsOnMethods = "closeDraft")
    public void verifyDraftsFolder() {
        DRAFTSFOLDER.click();
        Assert.assertTrue(isDisplayed(SAVEDDRAFT));
    }

    @Parameters({"ADDRESSEE", "SUBJECT", "FILENAME"})
    @Test(description = "Verify the draft content", dependsOnMethods = "verifyDraftsFolder")
    public void verifyLetterContent(String ADDRESSEE, String SUBJECT, String FILENAME) throws IOException {
        SAVEDDRAFT.click();
        waitForVisibilityFluently(LETTERBODY);
        Assert.assertTrue(SAVEDADDRESSEE.getAttribute("value").contains(ADDRESSEE));
        Assert.assertEquals(SAVEDSUBJECT.getAttribute("value"), SUBJECT);
        Assert.assertEquals(LETTERBODY.getText(), readFile(FILENAME));

    }

    @Test(description = "Send the mail", dependsOnMethods = "verifyLetterContent")
    public void sendLetter() {
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(SENDBUTTON));
        SENDBUTTON.click();
        waitForVisibilityExplicitly(SUCCESSMESSAGE);
        Assert.assertTrue(isDisplayed(SUCCESSMESSAGE));
    }


    @Test(description = "Verify, that the mail disappeared from ‘Drafts’ folder", dependsOnMethods = "sendLetter")
    public void confirmLetterDisapperared() {
        DRAFTSFOLDER.click();
        driver.navigate().refresh();
        waitForVisibilityFluently(COMPOSEBUTTON);
        Assert.assertFalse(isDisplayed(SAVEDDRAFT));
    }

    @Test(description = "Verify, that the mail is in ‘Sent’ folder", dependsOnMethods = "confirmLetterDisapperared")
    public void confirmLetterPresent() {
        SENTFOLDER.click();
        Assert.assertTrue(isDisplayed(SAVEDDRAFT));
    }

    @Test(description = "Log out", dependsOnMethods = "confirmLetterPresent")
    public void logOut() {
        ACCOUNTBUTTON.click();
        SIGNOUTBUTTON.click();
        Assert.assertTrue(isDisplayed(PASSWORDFIELD));

    }

    @AfterClass(description = "Close brwoser")
    public void tearDown() {
        driver.quit();
    }


    private String readFile(String fileName) throws IOException {
        byte[] encoded = readAllBytes(Paths.get(fileName));
        return new String(encoded);
    }

    private boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();

        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }

    }

    private void waitForVisibilityFluently(WebElement element) {
        new FluentWait(driver).withTimeout(30, TimeUnit.SECONDS).pollingEvery(5, TimeUnit.SECONDS)
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.visibilityOf(element));
    }

    private void waitForVisibilityExplicitly(WebElement element) {
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOf(element));
    }

    private void waitForInvisibilityFluently(WebElement element) {
        new FluentWait(driver).withTimeout(30, TimeUnit.SECONDS).pollingEvery(5, TimeUnit.SECONDS)
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.invisibilityOf(element));
    }


}