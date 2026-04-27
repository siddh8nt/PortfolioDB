USE portfoliodb;

-- Insert top US stocks while avoiding duplicates in existing assets table.
INSERT INTO assets (name, type)
SELECT s.name, 'Stock'
FROM (
    SELECT 'Amazon.com Inc.' AS name UNION ALL
    SELECT 'Alphabet Inc. Class A' UNION ALL
    SELECT 'Alphabet Inc. Class C' UNION ALL
    SELECT 'Nvidia Corp.' UNION ALL
    SELECT 'Meta Platforms Inc.' UNION ALL
    SELECT 'Berkshire Hathaway Inc. Class B' UNION ALL
    SELECT 'Tesla Inc.' UNION ALL
    SELECT 'Eli Lilly and Co.' UNION ALL
    SELECT 'Broadcom Inc.' UNION ALL
    SELECT 'JPMorgan Chase and Co.' UNION ALL
    SELECT 'Visa Inc.' UNION ALL
    SELECT 'Exxon Mobil Corp.' UNION ALL
    SELECT 'UnitedHealth Group Inc.' UNION ALL
    SELECT 'Mastercard Inc.' UNION ALL
    SELECT 'Walmart Inc.' UNION ALL
    SELECT 'Procter and Gamble Co.' UNION ALL
    SELECT 'Johnson and Johnson' UNION ALL
    SELECT 'Costco Wholesale Corp.' UNION ALL
    SELECT 'Home Depot Inc.' UNION ALL
    SELECT 'Oracle Corp.' UNION ALL
    SELECT 'Merck and Co. Inc.' UNION ALL
    SELECT 'AbbVie Inc.' UNION ALL
    SELECT 'Chevron Corp.' UNION ALL
    SELECT 'Coca-Cola Co.' UNION ALL
    SELECT 'PepsiCo Inc.' UNION ALL
    SELECT 'Adobe Inc.' UNION ALL
    SELECT 'Salesforce Inc.' UNION ALL
    SELECT 'Netflix Inc.' UNION ALL
    SELECT 'Cisco Systems Inc.' UNION ALL
    SELECT 'McDonald''s Corp.' UNION ALL
    SELECT 'AMD' UNION ALL
    SELECT 'Qualcomm Inc.' UNION ALL
    SELECT 'Thermo Fisher Scientific Inc.' UNION ALL
    SELECT 'Abbott Laboratories' UNION ALL
    SELECT 'Accenture plc' UNION ALL
    SELECT 'Intuit Inc.' UNION ALL
    SELECT 'IBM' UNION ALL
    SELECT 'Wells Fargo and Co.' UNION ALL
    SELECT 'Morgan Stanley' UNION ALL
    SELECT 'Goldman Sachs Group Inc.' UNION ALL
    SELECT 'Bank of America Corp.' UNION ALL
    SELECT 'American Express Co.' UNION ALL
    SELECT 'Pfizer Inc.' UNION ALL
    SELECT 'Danaher Corp.' UNION ALL
    SELECT 'Amgen Inc.' UNION ALL
    SELECT 'Texas Instruments Inc.' UNION ALL
    SELECT 'Union Pacific Corp.' UNION ALL
    SELECT 'Raytheon Technologies Corp.' UNION ALL
    SELECT 'Caterpillar Inc.' UNION ALL
    SELECT 'Deere and Co.' UNION ALL
    SELECT 'Honeywell International Inc.' UNION ALL
    SELECT 'Lowe''s Companies Inc.' UNION ALL
    SELECT 'Uber Technologies Inc.' UNION ALL
    SELECT 'BlackRock Inc.' UNION ALL
    SELECT 'S&P Global Inc.' UNION ALL
    SELECT 'Boeing Co.' UNION ALL
    SELECT 'Starbucks Corp.' UNION ALL
    SELECT 'ServiceNow Inc.' UNION ALL
    SELECT 'Booking Holdings Inc.' UNION ALL
    SELECT 'Applied Materials Inc.' UNION ALL
    SELECT 'Lam Research Corp.' UNION ALL
    SELECT 'Micron Technology Inc.' UNION ALL
    SELECT 'Intel Corp.' UNION ALL
    SELECT 'T-Mobile US Inc.' UNION ALL
    SELECT 'AT&T Inc.' UNION ALL
    SELECT 'Verizon Communications Inc.' UNION ALL
    SELECT 'Comcast Corp.' UNION ALL
    SELECT 'Charter Communications Inc.' UNION ALL
    SELECT 'Walt Disney Co.' UNION ALL
    SELECT 'PayPal Holdings Inc.' UNION ALL
    SELECT 'Block Inc.' UNION ALL
    SELECT 'Mondelez International Inc.' UNION ALL
    SELECT 'Philip Morris International Inc.' UNION ALL
    SELECT 'Altria Group Inc.' UNION ALL
    SELECT 'Bristol-Myers Squibb Co.' UNION ALL
    SELECT 'Gilead Sciences Inc.' UNION ALL
    SELECT 'Regeneron Pharmaceuticals Inc.' UNION ALL
    SELECT 'Vertex Pharmaceuticals Inc.' UNION ALL
    SELECT 'Medtronic plc' UNION ALL
    SELECT 'Nike Inc.' UNION ALL
    SELECT 'Target Corp.' UNION ALL
    SELECT 'Colgate-Palmolive Co.' UNION ALL
    SELECT 'General Electric Co.' UNION ALL
    SELECT 'General Motors Co.' UNION ALL
    SELECT 'Ford Motor Co.' UNION ALL
    SELECT 'Southern Co.' UNION ALL
    SELECT 'Duke Energy Corp.' UNION ALL
    SELECT 'NextEra Energy Inc.' UNION ALL
    SELECT 'ConocoPhillips' UNION ALL
    SELECT 'Schlumberger NV' UNION ALL
    SELECT 'EOG Resources Inc.' UNION ALL
    SELECT 'Marathon Petroleum Corp.' UNION ALL
    SELECT 'Chubb Ltd.' UNION ALL
    SELECT 'Progressive Corp.' UNION ALL
    SELECT 'Cigna Group' UNION ALL
    SELECT 'Elevance Health Inc.' UNION ALL
    SELECT 'CVS Health Corp.' UNION ALL
    SELECT 'Kraft Heinz Co.' UNION ALL
    SELECT 'KLA Corp.' UNION ALL
    SELECT 'Automatic Data Processing Inc.'
) s
LEFT JOIN assets a ON LOWER(a.name) = LOWER(s.name)
WHERE a.asset_id IS NULL;

-- Insert top cryptocurrencies while avoiding duplicates.
INSERT INTO assets (name, type)
SELECT c.name, 'Crypto'
FROM (
    SELECT 'BNB' AS name UNION ALL
    SELECT 'Solana' UNION ALL
    SELECT 'XRP' UNION ALL
    SELECT 'Cardano' UNION ALL
    SELECT 'Dogecoin' UNION ALL
    SELECT 'TRON' UNION ALL
    SELECT 'Toncoin' UNION ALL
    SELECT 'Avalanche' UNION ALL
    SELECT 'Chainlink' UNION ALL
    SELECT 'Polkadot' UNION ALL
    SELECT 'Polygon' UNION ALL
    SELECT 'Litecoin' UNION ALL
    SELECT 'Shiba Inu' UNION ALL
    SELECT 'Bitcoin Cash' UNION ALL
    SELECT 'Stellar'
) c
LEFT JOIN assets a ON LOWER(a.name) = LOWER(c.name)
WHERE a.asset_id IS NULL;

SELECT type, COUNT(*) AS total_assets_of_type
FROM assets
GROUP BY type;
