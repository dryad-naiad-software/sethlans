import {Mode} from "../../src/app/enums/mode.enum";

function checkSettings(mode: Mode) {
  cy.get('[id=ipAddress]').invoke('val').should('not.be.empty')
  cy.get('[id=networkPort]').invoke('val').should('not.be.empty')
  cy.get('[id=sethlansHostname]').invoke('val').should('not.be.empty')
  cy.get('[id=sethlansURL]').invoke('val').should('not.be.empty')
  cy.get('[id=logLevel]').select(1)
  if (mode === Mode.SERVER || mode === Mode.DUAL) {
    cy.get('[id=mailSettings]').click()
    cy.get('[id=smtpServer]').should('be.disabled')
    cy.get('[id=smtpPort]').should('be.disabled')
    cy.get('[id=replyToAddress]').should('be.disabled')
    cy.get('[id=smtpUser]').should('be.disabled')
    cy.get('[id=smtpPassword]').should('be.disabled')
    cy.get('[id=enableSMTP]').click()
    cy.get('[id=smtpServer]').should('be.enabled')
    cy.get('[id=smtpPort]').should('be.enabled')
    cy.get('[id=replyToAddress]').should('be.enabled')
  }

}

function configureUser(mode: Mode) {
  cy.get('[id=username]').type("sampleuser")
  cy.get('[id=password]').type("test1234")
  cy.get('[id=passwordConfirm]').type("test1234")
  if (mode === Mode.SERVER || mode === Mode.DUAL) {
    cy.get('[id=email]').type("sampleuser@example.com")
  }
  cy.get('[id=challenge1]').select(0)
  cy.get('[id=response1]').type("Something Interesting Part 1")
  cy.get('[id=challenge2]').select(2)
  cy.get('[id=response2]').type("Something Boring")
  cy.get('[id=challenge3]').select(3)
  cy.get('[id=response3]').type("simple")
}


describe('Sethlans Setup Wizard Tests', () => {

  it('Server Mode Test', () => {
    cy.visit('/')
    cy.contains('Sethlans Setup')
    cy.get('[id=start]').click()
    cy.get('[id=server]').click()
    cy.get('[id=next]').click()
    configureUser(Mode.SERVER)
    cy.get('[id=next]').click()
    cy.get('[id=blenderVersion]').select(0)
    cy.get('[id=next]').click()
    checkSettings(Mode.SERVER)

  });

  it('Node Mode Test', () => {
    cy.visit('/')
    cy.contains('Sethlans Setup')
    cy.get('[id=start]').click()
    cy.get('[id=node]').click()
    cy.get('[id=next]').click()
    configureUser(Mode.NODE)
    cy.get('[id=next]').click()

  });
})
