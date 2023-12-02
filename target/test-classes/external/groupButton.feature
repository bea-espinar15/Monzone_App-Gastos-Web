Feature: el grupo en user lleva a /group
  Background:
  * def delayTime = 1000

  @group_button
  Scenario: Pulsar un grupo en tu perfil
    Given call read('login.feature@login_b')
    And delay(delayTime)
    When click("#profile")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/user/config')
    And delay(delayTime)
    When click("#group-2")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/group/2')