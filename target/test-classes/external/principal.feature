Feature: flujo de la app
  Background:
  * def delayTime = 1000

  @group
  Scenario: Entrar a grupo
    Given call read('login.feature@login_b')
    And delay(delayTime)
    When click("#group-2 .card")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/group/2')

  @group_config
  Scenario: Configuracion grupo
    Given call read('principal.feature@group')
    And delay(delayTime)
    When click("#groupConfigBtn")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/group/2/config')

  @group_viewExpense
  Scenario: View expense in group
    Given call read('principal.feature@group')
    And delay(delayTime)
    When click(".body div.card")

  @profile
  Scenario: View profile
    Given call read('login.feature@login_b')
    And delay(delayTime)
    When click("#btn-drop")
    And delay(delayTime)
    When click("#profile")
    Then waitForUrl(baseUrl + '/user/config')