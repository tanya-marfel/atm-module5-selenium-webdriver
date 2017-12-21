import org.openqa.selenium.Keys;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GmailTest {
    private WebDriver driver;
    private static String URL = "https://gmail.com";
    private static String USERNAME = "tatsiana.marfel.test";
    private static String PASSWORD = "myNewPassword";
    private static String ADDRESSEE = "tatsianamarfel@gmail.com";
    private static String SUBJECT = "LoremIpsum";
    private static String FILENAME = "./src/main/resources/LoremIpsum.txt";


    @FindBy(xpath = "//input[@id=\"identifierId\"]")
    private WebElement USERNAMEFIELD;

    @FindBy(xpath = "//input[@name=\"password\"]")
    private WebElement PASSWORDFIELD;

    @FindBy(xpath = "//div[contains(text(), \"COMPOSE\")]")
    private WebElement COMPOSEBUTTON;

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
        // driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
    }

    @Test(description = "Sign in to Gmail account")
    public void loginToGmail() {
        driver.get(URL);
        USERNAMEFIELD.click();
        USERNAMEFIELD.sendKeys(USERNAME + Keys.ENTER);
        Assert.assertEquals(USERNAMEFIELD.getAttribute("data-initial-value"), USERNAME);
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOf(PASSWORDFIELD));
        PASSWORDFIELD.click();
        PASSWORDFIELD.sendKeys(PASSWORD + Keys.ENTER);

    }

    @Test(description = "Assert, that the login is successful", dependsOnMethods = "loginToGmail")
    public void confirmLoginSuccess() {

        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOf(COMPOSEBUTTON));
        Assert.assertTrue(isDisplayed(COMPOSEBUTTON));
    }

    @Test(description = "Create a new mail (fill addressee, subject and body fields)", dependsOnMethods = "confirmLoginSuccess")
    public void composeMail() {
        COMPOSEBUTTON.click();

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

    }

    @Test(description = "Verify, that the mail presents in ‘Drafts’ folder", dependsOnMethods = "closeDraft")
    public void verifyDraftsFolder() {
        DRAFTSFOLDER.click();
        Assert.assertTrue(isDisplayed(SAVEDDRAFT));
    }

    @Test(description = "Verify the draft content", dependsOnMethods = "verifyDraftsFolder")
    public void verifyLetterContent() throws IOException {
        SAVEDDRAFT.click();
        new FluentWait(driver).withTimeout(30, TimeUnit.SECONDS).pollingEvery(5, TimeUnit.SECONDS)
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.visibilityOf(LETTERBODY));
        // new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOf(SAVEDDRAFT));
        Assert.assertTrue(SAVEDADDRESSEE.getAttribute("value").contains(ADDRESSEE));

        Assert.assertEquals(SAVEDSUBJECT.getAttribute("value"), SUBJECT);

        Assert.assertEquals(LETTERBODY.getText(), readFile(FILENAME));

    }

    @Test(description = "Send the mail", dependsOnMethods = "verifyLetterContent")
    public void sendLetter() {
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(SENDBUTTON));
        SENDBUTTON.click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOf(SUCCESSMESSAGE));
    }


    @Test(description = "Verify, that the mail disappeared from ‘Drafts’ folder", dependsOnMethods = "sendLetter")
    public void confirmLetterDisapperared() {
        driver.navigate().refresh();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(isDisplayed(SAVEDDRAFT));
    }

    @Test(description = "Verify, that the mail is in ‘Sent’ folder", dependsOnMethods = "confirmLetterDisapperared")
    public void confirmLetterPresent() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SENTFOLDER.click();
        Assert.assertTrue(isDisplayed(SAVEDDRAFT));
    }

    @Test(description = "Log off", dependsOnMethods = "confirmLetterPresent")
    public void logOut() {
        ACCOUNTBUTTON.click();
        SIGNOUTBUTTON.click();
        Assert.assertTrue(isDisplayed(PASSWORDFIELD));

    }


    private String readFile(String fileName) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(fileName));
        return new String(encoded);
    }

    private boolean isDisplayed(WebElement element) {

        try {
            element.isDisplayed();
            return true;
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }

    }

}
