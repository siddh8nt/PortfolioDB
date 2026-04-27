package com.portfoliodb.dao;

import java.math.BigDecimal;

public record DashboardStats(
        int totalInvestors,
        BigDecimal assetsUnderManagement,
        int totalTransactions,
        BigDecimal averageDailyTradingVolume,
        String assetTypes,
        BigDecimal overallProfitLoss
) {
}
