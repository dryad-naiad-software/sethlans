import {Mode} from "../../src/app/enums/mode.enum";

function checkSettings(mode: Mode) {
  cy.get('#ipAddress').invoke('val').should('not.be.empty')
  cy.get('#networkPort').invoke('val').should('not.be.empty')
  cy.get('#sethlansHostname').invoke('val').should('not.be.empty')
  cy.get('#sethlansURL').invoke('val').should('not.be.empty')
  cy.get('#logLevel').select(1)
  if (mode === Mode.SERVER || mode === Mode.DUAL) {
    cy.get('#mailSettings').click()
    cy.get('#smtpServer').should('be.disabled')
    cy.get('#smtpPort').should('be.disabled')
    cy.get('#replyToAddress').should('be.disabled')
    cy.get('#smtpUser').should('be.disabled')
    cy.get('#smtpPassword').should('be.disabled')
    cy.get('#enableSMTP').click()
    cy.get('#smtpServer').should('be.enabled')
    cy.get('#smtpPort').should('be.enabled')
    cy.get('#replyToAddress').should('be.enabled')
    cy.get('#smtpServer').type('mail.example.com')
    cy.get('#smtpPort').type('1234')
    cy.get('#replyToAddress').type('noreply@example.com')
    cy.get('#useSSL').click()
    cy.get('#enableSTARTTLS').click()
    cy.get('#requireTLS').click()
    cy.get('#useSMTPAuth').should('not.be.checked')
    cy.get('#useSMTPAuth').click()
    cy.get('#smtpUser').type('sampleuser')
    cy.get('#smtpPassword').type('test1234')

  }

}

function configureUser(mode: Mode) {
  cy.get('#username').type("sampleuser")
  cy.get('#password').type("test1234")
  cy.get('#passwordConfirm').type("test1234")
  if (mode === Mode.SERVER || mode === Mode.DUAL) {
    cy.get('#email').type("sampleuser@example.com")
  }
  cy.get('#challenge1').select(0)
  cy.get('#response1').type("Something Interesting Part 1")
  cy.get('#challenge2').select(2)
  cy.get('#response2').type("Something Boring")
  cy.get('#challenge3').select(3)
  cy.get('#response3').type("simple")
}


describe('Sethlans Setup Wizard Tests', () => {

  it('Server Mode Test', () => {
    cy.visit('/')
    cy.contains('Sethlans Setup')
    cy.get('#start').click()
    cy.get('#server').click()
    cy.get('#next').click()
    configureUser(Mode.SERVER)
    cy.get('#next').click()
    cy.get('#blenderVersion').select(0)
    cy.get('#next').click()
    checkSettings(Mode.SERVER)
    cy.get('#next').click()

  });

  it('Node Mode Test', () => {
    cy.visit('/')
    cy.contains('Sethlans Setup')
    cy.get('#start').click()
    cy.get('#node').click()
    cy.get('#next').click()
    configureUser(Mode.NODE)
    cy.get('#next').click()

  });
})
