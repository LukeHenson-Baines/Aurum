import { useEffect, useState } from 'react'
import './App.css'

import {
  createAsset,
  createPortfolio,
  createTransaction,
  getAssets,
  getPortfolios,
  getPortfolioSummary,
  getPortfolioHoldings,
  getPortfolioTransactions,
} from './api/aurumApi'

function App() {
  const [portfolios, setPortfolios] = useState([])
  const [selectedPortfolioId, setSelectedPortfolioId] = useState(null)
  const [summary, setSummary] = useState(null)
  const [holdings, setHoldings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [assets, setAssets] = useState([])
  const [showTransactionForm, setShowTransactionForm] = useState(false)

  const [transactionForm, setTransactionForm] = useState({
    assetId: '',
    type: 'BUY',
    quantity: '',
    price: '',
    transactionDate: new Date().toISOString().split('T')[0],
  })

  const [showAssetForm, setShowAssetForm] = useState(false)

  const [assetForm, setAssetForm] = useState({
    symbol: '',
    name: '',
    currentPrice: '',
  })

  const [showPortfolioForm, setShowPortfolioForm] = useState(false)

  const [portfolioForm, setPortfolioForm] = useState({
    name: '',
  })

  const [transactions, setTransactions] = useState([])

  useEffect(() => {
    async function loadInitialData() {
      try {
        const [portfolioData, assetData] = await Promise.all([
          getPortfolios(),
          getAssets(),
        ])

        setPortfolios(portfolioData)
        setAssets(assetData)

        if (portfolioData.length > 0) {
          setSelectedPortfolioId(portfolioData[0].id)
        }
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    loadInitialData()
  }, [])

  useEffect(() => {
    if (!selectedPortfolioId) {
      return
    }

    async function loadPortfolioData() {
      try {
        setLoading(true)
        setError(null)

        await refreshPortfolioData(selectedPortfolioId)
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    loadPortfolioData()
  }, [selectedPortfolioId])

  async function refreshPortfolioData(portfolioId) {
    const [
      summaryData,
      holdingsData,
      transactionData,
    ] = await Promise.all([
      getPortfolioSummary(portfolioId),
      getPortfolioHoldings(portfolioId),
      getPortfolioTransactions(portfolioId),
    ])

    setSummary(summaryData)
    setHoldings(holdingsData)
    setTransactions(transactionData)
  }

  function handlePortfolioChange(event) {
    setPortfolioForm({
      name: event.target.value,
    })
  }

  async function handlePortfolioSubmit(event) {
    event.preventDefault()

    try {
      setError(null)

      const newPortfolio = await createPortfolio({
        name: portfolioForm.name,
      })

      const updatedPortfolios = await getPortfolios()

      setPortfolios(updatedPortfolios)
      setSelectedPortfolioId(newPortfolio.id)

      setPortfolioForm({
        name: '',
      })

      setShowPortfolioForm(false)
    } catch (err) {
      setError(err.message)
    }
  }

  function formatCurrency(value) {
    return new Intl.NumberFormat('en-GB', {
      style: 'currency',
      currency: 'GBP',
    }).format(value ?? 0)
  }

  function formatPercentage(value) {
    return `${Number(value ?? 0).toFixed(2)}%`
  }

  function handleTransactionChange(event) {
    const { name, value } = event.target

    setTransactionForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

  async function handleTransactionSubmit(event) {
    event.preventDefault()

    try {
      setError(null)

      await createTransaction(selectedPortfolioId, {
        assetId: Number(transactionForm.assetId),
        type: transactionForm.type,
        quantity: Number(transactionForm.quantity),
        price: Number(transactionForm.price),
        transactionDate: transactionForm.transactionDate,
      })

      await refreshPortfolioData(selectedPortfolioId)

      setTransactionForm({
        assetId: '',
        type: 'BUY',
        quantity: '',
        price: '',
        transactionDate: new Date().toISOString().split('T')[0],
      })

      setShowTransactionForm(false)
    } catch (err) {
      setError(err.message)
    }
  }

  function handleAssetChange(event) {
    const { name, value } = event.target

    setAssetForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

  async function handleAssetSubmit(event) {
    event.preventDefault()

    try {
      setError(null)

      await createAsset({
        symbol: assetForm.symbol.toUpperCase(),
        name: assetForm.name,
        currentPrice: Number(assetForm.currentPrice),
      })

      const updatedAssets = await getAssets()
      setAssets(updatedAssets)

      setAssetForm({
        symbol: '',
        name: '',
        currentPrice: '',
      })

      setShowAssetForm(false)
    } catch (err) {
      setError(err.message)
    }
  }

  function formatDate(value) {
    if (!value) {
      return ''
    }

    return new Intl.DateTimeFormat('en-GB', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    }).format(new Date(`${value}T00:00:00`))
  }

  function valueClass(value) {
    const numericValue = Number(value ?? 0)

    if (numericValue > 0) {
      return 'positive'
    }

    if (numericValue < 0) {
      return 'negative'
    }

    return ''
  }

  function signedCurrency(value) {
    const numericValue = Number(value ?? 0)

    const formatted = formatCurrency(Math.abs(numericValue))

    if (numericValue > 0) {
      return `+${formatted}`
    }

    if (numericValue < 0) {
      return `-${formatted}`
    }

    return formatted
  }

  if (loading && portfolios.length === 0) {
    return <div className="status-screen">Loading Aurum...</div>
  }

  if (error && portfolios.length === 0) {
    return <div className="status-screen error">{error}</div>
  }

  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>Aurum</h1>
          <p>Investment Portfolio Tracker</p>
        </div>
      </header>

      <main className="dashboard">
        <section className="dashboard-header">
          <div>
            <p className="eyebrow">PORTFOLIO</p>
            <h2>{summary?.portfolioName ?? 'Portfolio Overview'}</h2>

            {portfolios.length > 0 && (
              <select
                className="portfolio-select"
                value={selectedPortfolioId ?? ''}
                onChange={(event) =>
                  setSelectedPortfolioId(Number(event.target.value))
                }
              >
                {portfolios.map((portfolio) => (
                  <option
                    key={portfolio.id}
                    value={portfolio.id}
                  >
                    {portfolio.name}
                  </option>
                ))}
              </select>
            )}
          </div>

          <div className="header-actions">
            <button
              className="secondary-button"
              onClick={() => setShowPortfolioForm(true)}
            >
              Add Portfolio
            </button>

            <button
              className="secondary-button"
              onClick={() => setShowAssetForm(true)}
            >
              Add Asset
            </button>

            <button
              className="primary-button"
              onClick={() => setShowTransactionForm(true)}
            >
              Add Transaction
            </button>
          </div>

        </section>

        {loading && portfolios.length > 0 && (
          <div className="loading-bar">
            Updating portfolio...
          </div>
        )}

        {error && (
          <div className="error-banner">
            {error}
          </div>
        )}

        {showPortfolioForm && (
          <section className="transaction-panel">
            <div className="panel-header transaction-header">
              <h3>Add Portfolio</h3>

              <button
                className="secondary-button"
                onClick={() => setShowPortfolioForm(false)}
              >
                Cancel
              </button>
            </div>

            <form
              className="portfolio-form"
              onSubmit={handlePortfolioSubmit}
            >
              <label>
                Portfolio Name
                <input
                  name="name"
                  type="text"
                  placeholder="Growth Portfolio"
                  value={portfolioForm.name}
                  onChange={handlePortfolioChange}
                  required
                />
              </label>

              <button
                className="primary-button"
                type="submit"
              >
                Create Portfolio
              </button>
            </form>
          </section>
        )}

        {showAssetForm && (
          <section className="transaction-panel">
            <div className="panel-header transaction-header">
              <h3>Add Asset</h3>

              <button
                className="secondary-button"
                onClick={() => setShowAssetForm(false)}
              >
                Cancel
              </button>
            </div>

            <form
              className="asset-form"
              onSubmit={handleAssetSubmit}
            >
              <label>
                Symbol
                <input
                  name="symbol"
                  type="text"
                  placeholder="AAPL"
                  value={assetForm.symbol}
                  onChange={handleAssetChange}
                  required
                />
              </label>

              <label>
                Name
                <input
                  name="name"
                  type="text"
                  placeholder="Apple Inc."
                  value={assetForm.name}
                  onChange={handleAssetChange}
                  required
                />
              </label>

              <label>
                Current Price
                <input
                  name="currentPrice"
                  type="number"
                  min="0"
                  step="0.0001"
                  placeholder="225.50"
                  value={assetForm.currentPrice}
                  onChange={handleAssetChange}
                  required
                />
              </label>

              <button
                className="primary-button"
                type="submit"
              >
                Save Asset
              </button>
            </form>
          </section>
        )}

        {showTransactionForm && (
          <section className="transaction-panel">
            <div className="panel-header transaction-header">
              <h3>Add Transaction</h3>

              <button
                className="secondary-button"
                onClick={() => setShowTransactionForm(false)}
              >
                Cancel
              </button>
            </div>

            <form
              className="transaction-form"
              onSubmit={handleTransactionSubmit}
            >
              <label>
                Asset
                <select
                  name="assetId"
                  value={transactionForm.assetId}
                  onChange={handleTransactionChange}
                  required
                >
                  <option value="">Select asset</option>

                  {assets.map((asset) => (
                    <option key={asset.id} value={asset.id}>
                      {asset.symbol} — {asset.name}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Type
                <select
                  name="type"
                  value={transactionForm.type}
                  onChange={handleTransactionChange}
                >
                  <option value="BUY">Buy</option>
                  <option value="SELL">Sell</option>
                </select>
              </label>

              <label>
                Quantity
                <input
                  name="quantity"
                  type="number"
                  min="0"
                  step="0.00000001"
                  value={transactionForm.quantity}
                  onChange={handleTransactionChange}
                  required
                />
              </label>

              <label>
                Price
                <input
                  name="price"
                  type="number"
                  min="0"
                  step="0.0001"
                  value={transactionForm.price}
                  onChange={handleTransactionChange}
                  required
                />
              </label>

              <label>
                Date
                <input
                  name="transactionDate"
                  type="date"
                  value={transactionForm.transactionDate}
                  onChange={handleTransactionChange}
                  required
                />
              </label>

              <button
                className="primary-button"
                type="submit"
              >
                Save Transaction
              </button>
            </form>
          </section>
        )}

        <section className="summary-grid">
          <div className="summary-card">
            <span>Portfolio Value</span>
            <strong>
              {formatCurrency(summary?.totalMarketValue)}
            </strong>

            <small>
              Cost basis {formatCurrency(summary?.totalCostBasis)}
            </small>
          </div>

          <div className="summary-card">
            <span>Total P&amp;L</span>

            <strong className={valueClass(summary?.totalProfitLoss)}>
              {signedCurrency(summary?.totalProfitLoss)}
            </strong>

            <small>
              Realised{' '}
              <span className={valueClass(summary?.totalRealisedProfitLoss)}>
                {signedCurrency(summary?.totalRealisedProfitLoss)}
              </span>
            </small>
          </div>

          <div className="summary-card">
            <span>Return</span>

            <strong className={valueClass(summary?.returnPercentage)}>
              {Number(summary?.returnPercentage ?? 0) > 0 ? '+' : ''}
              {formatPercentage(summary?.returnPercentage)}
            </strong>

            <small>
              Unrealised{' '}
              <span className={valueClass(summary?.totalUnrealisedProfitLoss)}>
                {signedCurrency(summary?.totalUnrealisedProfitLoss)}
              </span>
            </small>
          </div>

          <div className="summary-card">
            <span>Positions</span>
            <strong>{holdings.length}</strong>

            <small>
              Active holdings
            </small>
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <h3>Holdings</h3>
          </div>

          {holdings.length === 0 ? (
            <div className="empty-state">
              <strong>No holdings yet</strong>
              <span>
                Add a BUY transaction to start tracking this portfolio.
              </span>
            </div>
          ) : (
            <div className="table-wrapper">
              <table className="holdings-table">
                <thead>
                  <tr>
                    <th>Asset</th>
                    <th>Quantity</th>
                    <th>Avg. Cost</th>
                    <th>Current Price</th>
                    <th>Market Value</th>
                    <th>Unrealised P&amp;L</th>
                    <th>Realised P&amp;L</th>
                  </tr>
                </thead>

                <tbody>
                  {holdings.map((holding) => (
                    <tr key={holding.assetId}>
                      <td>
                        <strong>{holding.symbol}</strong>
                        <span className="asset-name">
                          {holding.name}
                        </span>
                      </td>

                      <td>{Number(holding.quantity)}</td>

                      <td>
                        {formatCurrency(holding.averageCost)}
                      </td>

                      <td>
                        {formatCurrency(holding.currentPrice)}
                      </td>

                      <td>
                        {formatCurrency(holding.marketValue)}
                      </td>

                      <td
                        className={valueClass(
                          holding.unrealisedProfitLoss
                        )}
                      >
                        {signedCurrency(
                          holding.unrealisedProfitLoss
                        )}
                      </td>

                      <td
                        className={valueClass(
                          holding.realisedProfitLoss
                        )}
                      >
                        {signedCurrency(
                          holding.realisedProfitLoss
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
        <section className="panel transaction-history-panel">
          <div className="panel-header">
            <h3>Transaction History</h3>
          </div>

          {transactions.length === 0 ? (
            <div className="empty-state">
              No transactions to display.
            </div>
          ) : (
            <div className="table-wrapper">
              <table className="holdings-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Asset</th>
                    <th>Type</th>
                    <th>Quantity</th>
                    <th>Price</th>
                    <th>Total</th>
                  </tr>
                </thead>

                <tbody>
                  {[...transactions]
                    .reverse()
                    .map((transaction) => (
                      <tr key={transaction.id}>
                        <td>
                          {formatDate(transaction.transactionDate)}
                        </td>

                        <td>
                          <strong>{transaction.assetSymbol}</strong>
                        </td>

                        <td>
                          <span
                            className={`transaction-type ${
                              transaction.type === 'BUY'
                                ? 'transaction-buy'
                                : 'transaction-sell'
                            }`}
                          >
                            {transaction.type}
                          </span>
                        </td>

                        <td>
                          {Number(transaction.quantity)}
                        </td>

                        <td>
                          {formatCurrency(transaction.price)}
                        </td>

                        <td>
                          {formatCurrency(
                            Number(transaction.quantity) *
                              Number(transaction.price)
                          )}
                        </td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </div>
  )
}

export default App