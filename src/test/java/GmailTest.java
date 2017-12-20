import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class GmailTest {
    private WebDriver driver;
    private static String URL = "https://gmail.com";
    private static String USERNAMEFIELD = "//input[@id=\"identifierId\"]";
    private static String USERNAME = "tatsiana.marfel.test";
    private static String PASSWORDFIELD = "//input[@name=\"password\"]";
    private static String PASSWORD = "myNewPassword";
    private static String COMPOSEBUTTON = "//div[contains(text(), \"COMPOSE\")]";
    private static String ADDRESSEEFIELD = "//textarea[@name=\"to\"]";
    private static String ADDRESSEE = "tatsianamarfel@gmail.com";

    private static String SUBJECTFIELD = "subjectbox";
    private static String SUBJECT = "LoremIpsum";
    private static String BODYFIELD = "//div[@role=\"textbox\"]";
    private static String CLOSEBUTTON = "//img[@alt=\"Close\"]";
    private static String FILENAME = "/Users/tatsiana_marfel/IdeaProjects/GmailTest/src/main/resources/LoremIpsum.txt";
    private static String SUBJECTINPUT = "//input[@name=\"subject\"]";git 
    private static String ADDRESSEEINPUT = "//input[@name=\"to\"]";
    private static String MESSAGEBODY = "div[aria-label='Message Body']";
    private static String DRAFTSLINK = "Drafts";
    private static String SAVEDDRAFT = "//span[contains(text(), 'LoremIpsum')]";
    private static String SENDBUTTON = "//div[contains(text(), \"Send\")]";
    private static String SENTFOLDER = "a[title=\"Sent Mail\"]";
    private static String SUCCESSMESSAGE = "//div[contains(text(), \"Your message has been sent\")]";
    private WebElement newLetter;
    private WebElement to;
    private WebElement subject;
    private WebElement body;
    private WebElement closeButton;
    private WebElement drafts;
    private WebElement sendButton;
    //    private WebElement savedDraft;
    private WebElement subjectSaved;
    private WebElement addresseeSaved;
    private WebElement bodySaved;
    private WebElement sent;

    @BeforeClass(description = "Launch browser")
    public void launchBrowser() {

        System.setProperty("webdriver.chrome.driver", "/Volumes/DATA/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        driver = new ChromeDriver();
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
        WebElement user = driver.findElement(By.xpath(USERNAMEFIELD));
        user.click();
        user.sendKeys(USERNAME + Keys.ENTER);

        Assert.assertEquals(user.getAttribute("data-initial-value"), USERNAME);

        WebElement pass = new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(PASSWORDFIELD)));
        pass.click();
        pass.sendKeys(PASSWORD + Keys.ENTER);

    }

    @Test(description = "Assert, that the login is successful", dependsOnMethods = "loginToGmail")
    public void confirmLoginSuccess() {
        newLetter = new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath(COMPOSEBUTTON)));
        Assert.assertTrue(!driver.findElements(By.xpath(COMPOSEBUTTON)).isEmpty());
    }

    @Test(description = "Create a new mail (fill addressee, subject and body fields)", dependsOnMethods = "confirmLoginSuccess")
    public void composeMail() throws IOException, InterruptedException {
        newLetter = driver.findElement(By.xpath(COMPOSEBUTTON));
        newLetter.click();

        to = driver.findElement(By.xpath(ADDRESSEEFIELD));
        to.click();
        to.sendKeys(ADDRESSEE + Keys.ENTER);

        subject = driver.findElement(By.name(SUBJECTFIELD));
        subject.click();
        subject.sendKeys(SUBJECT + Keys.TAB);


        body = driver.findElement(By.xpath(BODYFIELD));
        body.click();
        body.sendKeys(readFile(FILENAME));


    }

    @Test(description = "Closing the written letter (as an alternative to saving draft)", dependsOnMethods = "composeMail")
    public void closeDraft() {
        closeButton = driver.findElement(By.xpath(CLOSEBUTTON));
        closeButton.click();

    }

    @Test(description = "Verify, that the mail presents in ‘Drafts’ folder", dependsOnMethods = "closeDraft")
    public void verifyDraftsFolder() {
        drafts = driver.findElement(By.partialLinkText(DRAFTSLINK));
        drafts.click();
        Assert.assertTrue(!driver.findElements(By.xpath(SAVEDDRAFT)).isEmpty());
    }

    @Test(description = "Verify the draft content", dependsOnMethods = "verifyDraftsFolder")
    public void verifyLetterContent() throws IOException {
        driver.findElement(By.xpath(SAVEDDRAFT)).click();
        addresseeSaved = driver.findElement(By.xpath(ADDRESSEEINPUT));
        Assert.assertEquals(addresseeSaved.getAttribute("value"), ADDRESSEE);

        subjectSaved = driver.findElement(By.xpath(SUBJECTINPUT));
        Assert.assertEquals(subjectSaved.getAttribute("value"), SUBJECT);

        bodySaved = driver.findElement(By.cssSelector(MESSAGEBODY));

        Assert.assertEquals(bodySaved.getText(), readFile(FILENAME));

    }

    @Test(description = "Send the mail", dependsOnMethods = "verifyLetterContent")
    public void sendLetter() throws InterruptedException {
        sendButton = new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.xpath(SENDBUTTON)));
        sendButton.click();
        sendButton = new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath(SUCCESSMESSAGE)));
    }


    @Test(description = "Verify, that the mail disappeared from ‘Drafts’ folder", dependsOnMethods = "sendLetter")
    public void confirmLetterDisapperared() {
        driver.navigate().refresh();
        Assert.assertTrue(driver.findElements(By.xpath(SAVEDDRAFT)).isEmpty());
    }

    @Test(description = "Verify, that the mail is in ‘Sent’ folder", dependsOnMethods = "confirmLetterDisapperared")
    public void confirmLetterPresent() {
        sent = driver.findElement(By.cssSelector(SENTFOLDER));
        sent.click();
        Assert.assertTrue(!driver.findElements(By.xpath(SAVEDDRAFT)).isEmpty());
    }


    private String readFile(String fileName) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(fileName));
        return new String(encoded);
    }

}
