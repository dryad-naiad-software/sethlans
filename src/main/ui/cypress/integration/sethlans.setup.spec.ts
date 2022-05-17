describe('Sethlans Setup Wizard Tests', () => {

  it('Server Mode Test', () => {
    cy.visit('/')
    cy.contains('Sethlans Setup')
    cy.get('[id=start]').click()
    cy.get('[id=server]').click()
    cy.get('[id=next]').click()
    cy.get('[id=username]').type("texhound")
    cy.get('[id=password]').type("test1234")
    cy.get('[id=passwordConfirm]').type("test1234")

  });

  it('Node Mode Test', () => {
    cy.visit('/')
    cy.contains('Sethlans Setup')
    cy.get('[id=start]').click()
    cy.get('[id=node]').click()
    cy.get('[id=next]').click()
    cy.get('[id=username]').type("texhound")
    cy.get('[id=password]').type("test1234")
    cy.get('[id=passwordConfirm]').type("test1234")

  });
})
