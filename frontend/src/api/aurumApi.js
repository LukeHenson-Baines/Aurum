const API_BASE_URL = 'http://localhost:8080/api'

export async function getPortfolios() {
  const response = await fetch(`${API_BASE_URL}/portfolios`)

  if (!response.ok) {
    throw new Error('Failed to load portfolios')
  }

  return response.json()
}

export async function getPortfolioSummary(portfolioId) {
  const response = await fetch(
    `${API_BASE_URL}/portfolios/${portfolioId}/summary`
  )

  if (!response.ok) {
    throw new Error('Failed to load portfolio summary')
  }

  return response.json()
}

export async function getPortfolioHoldings(portfolioId) {
  const response = await fetch(
    `${API_BASE_URL}/portfolios/${portfolioId}/holdings`
  )

  if (!response.ok) {
    throw new Error('Failed to load portfolio holdings')
  }

  return response.json()
}