# Travel Budget Tracker Release Notes

## Version 2.2.0

### Highlights
- Redesigned the app into a cleaner premium-style interface with a dedicated intro animation.
- Added a new travel budget logo for the intro, app icon, and native splash styling.
- Added trip deletion from the home screen with long-press confirmation.
- Added sideways swipe pages in the trip dashboard: Expenses, Debts, and Owed to me.
- Added category filters for All, Food, Travel, Stay, Shopping, and Other.
- Summary totals now update based on the selected category filter.

### Expenses
- Added expense details view on tap.
- Added long-press delete for expenses.
- Added dynamic split people support with compact add and remove controls.
- Added paid/unpaid tracking for people who owe you money.
- Paid reimbursements reduce the visible net total spent.
- Added support for attaching up to 3 receipt/photos per expense.

### Debts
- Added Add Debt flow.
- Added debt paid/unpaid toggle.
- Added long-press delete for debts.

### CSV
- Added CSV import for expenses.
- Added CSV export from the top-right export icon.
- CSV export respects the currently selected category filter.
- Export format is compatible with Google Sheets.

### UI Polish
- Removed noisy dashboard labels and duplicate add actions.
- Simplified home trip cards.
- Added compact controls for split and paid actions.
- Improved Owed to me row layout to prevent amount/name overlap.
- Removed the white native splash flash by using a dark splash background.
