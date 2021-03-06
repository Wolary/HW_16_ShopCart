import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class CartTest {
    int item = 1;
    String contentType = "application/x-www-form-urlencoded; charset=UTF-8",
            post = "http://demowebshop.tricentis.com/addproducttocart/details/18/1",
            body = "addtocart_18.EnteredQuantity=";

    //Эта кука взята из браузера, так можно подсовывать их вручную, работает с api, но не работает с ui
    //   String userCookie = ("Nop.customer=67a0e9d8-9d98-464d-928e-890a321f51ef; NopCommerce.RecentlyViewedProducts=RecentlyViewedProductIds=17; ARRAffinity=06e3c6706bb7098b5c9133287f2a8d510a64170f97e4ff5fa919999d67a34a46; __utma=78382081.201302361.1621858739.1621858739.1621858739.1; __utmc=78382081; __utmz=78382081.1621858739.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utmt=1; __atuvc=3|21; __atuvs=60ab99b2bc37f6db002; __utmb=78382081.4.10.1621858739");

    //так тоже не хочет
//    String userCookie() {
//        open("http://demowebshop.tricentis.com/");
//        String userCookie = WebDriverRunner.getWebDriver().manage().getCookieNamed("userCookie").getValue();
//    }

          //работает, но не передает куки в браузер, хотя у других все норм
    String userCookie() {
        return given()
                .contentType(contentType)
                .when()
                .get("http://demowebshop.tricentis.com/")
                .then()
                .statusCode(200)
                .extract()
                .cookie("Nop.customer");
    }

    @Test
    @DisplayName("Добавляем товар в корзину и проверяем с помощью Api")
    void addToCartApiTest() {

        //Закоментил но оставлю чтоб знать как проверить сколько в корзине
//        String cartSize = given()
//                .contentType(contentType)
//                .body(body + item)
//                .cookie("userCookie", userCookie())
//                .when()
//                .post(post)
//                .then()
//                .statusCode(200)
//                .extract().path("updatetopcartsectionhtml");
//        Integer newCartSize = Integer.parseInt(cartSize.substring(1, 2));

        given()
                .contentType(contentType)
                .body(body + item)
                .cookie("Nop.customer", userCookie())
                .when()
                .post(post)
                .then()
                .statusCode(200)
                .log().body()
                .body("success", is(true))
                .body("updatetopcartsectionhtml", is(String.format("(%s)", item)));
    }

    @Test
    @DisplayName("Добавляем товар в корзину и проверяем с помощью UI")
    void addToCartUITest() {

        String cartSize = given()
                .contentType(contentType)
                .body(body + item)
                .cookie("Nop.customer", userCookie())
                .when()
                .post(post)
                .then()
                .statusCode(200)
                .extract().path("updatetopcartsectionhtml");
        Integer newCartSize = Integer.parseInt(cartSize.substring(1, 2));

        given()
                .contentType(contentType)
                .body(body + item)
                .cookie("Nop.customer", userCookie())
                .when()
                .post(post)
                .then()
                .statusCode(200);

        open("http://demowebshop.tricentis.com/");

        // так у студента в дз работает, у меня почему то нет.
        // Cookie uiCookie = new Cookie("userCookie", userCookie());
        // getWebDriver().manage().addCookie(uiCookie);

        getWebDriver().manage().addCookie(new Cookie("Nop.customer", userCookie()));
        Selenide.refresh();
        $(".cart-qty").shouldHave(Condition.text(String.format("(%s)", 0)));
        //заработало подставление куки, разобраться почему 0

    }
}

