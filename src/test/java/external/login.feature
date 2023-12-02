Feature: login en servidor
  Background:
  * def delayTime = 2000

  @login_b
  Scenario: login correcto como b
    Given driver baseUrl
    And delay(delayTime)
    And input('#username', 'b')
    And input('#password', 'aa')
    When click("#btn-signin")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/user')

  @login_a
  Scenario: login correcto como a
    Given driver baseUrl
    And delay(delayTime)
    And input('#username', 'a')
    And input('#password', 'aa')
    When click("#btn-signin")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/admin')

  @login_Tester
  Scenario: login correcto como Tester
    Given driver baseUrl
    And delay(delayTime)
    And input('#username', 'Tester')
    And input('#password', 'aa')
    When click("#btn-signin")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/user')

  @logout
  Scenario: logout
    When click("#btn-drop")
    And delay(delayTime)
    When click("#btn-logout")
    Then waitForUrl(baseUrl + '/login')