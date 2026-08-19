package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.vo.OrderReportVO;
import com.atguigu.oline_trading_platform.vo.SalesTop10ReportVO;
import com.atguigu.oline_trading_platform.vo.TurnoverReportVO;
import com.atguigu.oline_trading_platform.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;

public interface ReportService {

    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    OrderReportVO ordersStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);

    void exportBusinessData(HttpServletResponse response);
}
