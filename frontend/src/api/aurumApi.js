const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

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

export async function createTransaction(portfolioId, transaction) {
  const response = await fetch(
    `${API_BASE_URL}/portfolios/${portfolioId}/transactions`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(transaction),
    }
  )

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null)

    throw new Error(
      errorBody?.message ?? 'Failed to create transaction'
    )
  }

  return response.json()
}

export async function getAssets() {
  const response = await fetch(`${API_BASE_URL}/assets`)

  if (!response.ok) {
    throw new Error('Failed to load assets')
  }

  return response.json()
}

export async function createAsset(asset) {
  const response = await fetch(`${API_BASE_URL}/assets`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(asset),
  })

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null)

    throw new Error(
      errorBody?.message ?? 'Failed to create asset'
    )
  }

  return response.json()
}

export async function createPortfolio(portfolio) {
  const response = await fetch(`${API_BASE_URL}/portfolios`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(portfolio),
  })

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null)

    throw new Error(
      errorBody?.message ?? 'Failed to create portfolio'
    )
  }

  return response.json()
}

export async function getPortfolioTransactions(portfolioId) {
  const response = await fetch(
    `${API_BASE_URL}/portfolios/${portfolioId}/transactions`
  )

  if (!response.ok) {
    throw new Error('Failed to load portfolio transactions')
  }

  return response.json()
}