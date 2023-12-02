Feature: uso de la app
  Background:
  * def delayTime = 1000

  @new_group
  Scenario: Crear un grupo nuevo
    Given call read('login.feature@login_Tester')
    And delay(delayTime)
    When click("#btn-newGroup")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/group/new')
    And delay(delayTime)
    And input('#name', 'Karate Group')
    And input('#desc', 'Auto generated description')
    And select('#sel-currency', 1)
    And input('#budget', '10')
    And click("#btn-save")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/user')
    And click("#groupsTable .card") 
    And delay(delayTime)
    When click("#groupConfigBtn")
    And delay(delayTime)
    #Then waitForUrl(baseUrl + '/group/2/config')
    Then match driver.value('#name') == 'Karate Group'
    Then match driver.value('#desc') == 'Auto generated description'
    Then match driver.value('#sel-currency') == '0'
    Then match driver.value('#budget') == '10.0'
    And delay(delayTime)

  @group_deleteExpense
  Scenario: Delete group expense
    Given call read('principal.feature@group')
    And delay(delayTime)
    When click("#expensesTable .card")
    Then waitForUrl(baseUrl + '/group/2/99')
    And delay(delayTime)
    And click("#btn-delete")
    And delay(delayTime)
    And click("#confirmBtn")
    Then waitForUrl(baseUrl + '/group/2')

  @group_addExpense
  Scenario: Add expense to group
    Given call read('principal.feature@group')
    And delay(delayTime)
    When click("#addExpense")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/group/2/new')
    And delay(delayTime)
    And input('#name', 'Karate Expense')
    And input('#desc', 'Auto generated description')
    And input('#dateString', '01-01-2023')
    And input('#amount', '42')
    And select('#paidById', 1)
    And select('#typeId', 1)
    And click("#btn-save")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/group/2')
    # Obtiene el primerExpense
    And click("#expensesTable .card")
    And delay(delayTime)
    # Comprobar que los campos son correctos
    Then match driver.value('#name') == 'Karate Expense'
    Then match driver.value('#desc') == 'Auto generated description'
    Then match driver.value('#dateString') == '2023-01-01'
    Then match driver.value('#amount') == '42.0'
    Then match driver.value('#paidById') == '2'
    Then match driver.value('#typeId') == '1'

  @invitationAndJoin
  Scenario: Invitar a un grupo y unirse
    Given call read('principal.feature@group_config')
    And delay(delayTime)
    And click('#inviteBtn')
    And delay(delayTime)
    And input('#inviteUsername', 'Tester')
    And click("#confirmInviteBtn")
    And delay(delayTime)
    Given call read('login.feature@logout')
    Given call read('login.feature@login_Tester')
    And delay(delayTime)
    And click("#notifB")
    And click("#action-notifs-tab")
    And click("#btn-accept")
    And click("#btn-close")
    When click("#group-2 .card")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/group/2')