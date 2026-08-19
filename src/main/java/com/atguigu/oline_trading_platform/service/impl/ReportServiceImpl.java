package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.dto.GoodsSalesDTO;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.mapper.OrderMapper;
import com.atguigu.oline_trading_platform.mapper.UserMapper;
import com.atguigu.oline_trading_platform.service.ReportService;
import com.atguigu.oline_trading_platform.service.WorkspaceService;
import com.atguigu.oline_trading_platform.vo.BusinessDataVO;
import com.atguigu.oline_trading_platform.vo.OrderReportVO;
import com.atguigu.oline_trading_platform.vo.SalesTop10ReportVO;
import com.atguigu.oline_trading_platform.vo.TurnoverReportVO;
import com.atguigu.oline_trading_platform.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = dateList(begin, end);
        List<String> dateStr = new ArrayList<>();
        List<String> turnoverStr = new ArrayList<>();
        for (LocalDate date : dates) {
            dateStr.add(date.toString());
            BigDecimal turnover = orderMapper.sumTurnover(date.atStartOfDay(), endOfDay(date));
            turnoverStr.add(turnover == null ? "0" : turnover.toPlainString());
        }
        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateStr))
                .turnoverList(String.join(",", turnoverStr))
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = dateList(begin, end);
        List<String> dateStr = new ArrayList<>();
        List<String> newUserStr = new ArrayList<>();
        List<String> totalUserStr = new ArrayList<>();
        for (LocalDate date : dates) {
            dateStr.add(date.toString());
            Integer newUsers = userMapper.countNewUsers(date.atStartOfDay(), endOfDay(date));
            Integer totalUsers = userMapper.countTotalUsers(endOfDay(date));
            newUserStr.add(String.valueOf(newUsers == null ? 0 : newUsers));
            totalUserStr.add(String.valueOf(totalUsers == null ? 0 : totalUsers));
        }
        return UserReportVO.builder()
                .dateList(String.join(",", dateStr))
                .newUserList(String.join(",", newUserStr))
                .totalUserList(String.join(",", totalUserStr))
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = dateList(begin, end);
        List<String> dateStr = new ArrayList<>();
        List<String> orderCountStr = new ArrayList<>();
        List<String> validCountStr = new ArrayList<>();
        int total = 0;
        int valid = 0;
        for (LocalDate date : dates) {
            dateStr.add(date.toString());
            Integer orderCount = defaultInt(orderMapper.countByTime(date.atStartOfDay(), endOfDay(date)));
            Integer validCount = defaultInt(orderMapper.countValidByTime(date.atStartOfDay(), endOfDay(date)));
            orderCountStr.add(String.valueOf(orderCount));
            validCountStr.add(String.valueOf(validCount));
            total += orderCount;
            valid += validCount;
        }
        double rate = total == 0 ? 0D : valid * 1.0 / total;
        return OrderReportVO.builder()
                .dateList(String.join(",", dateStr))
                .orderCountList(String.join(",", orderCountStr))
                .validOrderCountList(String.join(",", validCountStr))
                .totalOrderCount(total)
                .validOrderCount(valid)
                .orderCompletionRate(rate)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> list = orderMapper.top10(begin.atStartOfDay(), endOfDay(end));
        if (list == null) {
            list = List.of();
        }
        return SalesTop10ReportVO.builder()
                .nameList(list.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(",")))
                .numberList(list.stream().map(item -> String.valueOf(item.getNumber())).collect(Collectors.joining(",")))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDate begin = end.minusDays(29);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("运营数据");
            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue("运营数据报表（近30天）");

            Row header = sheet.createRow(2);
            header.createCell(0).setCellValue("日期");
            header.createCell(1).setCellValue("营业额");
            header.createCell(2).setCellValue("有效订单");
            header.createCell(3).setCellValue("订单完成率");
            header.createCell(4).setCellValue("平均客单价");
            header.createCell(5).setCellValue("新增用户");

            int rowNum = 3;
            for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
                BusinessDataVO data = workspaceService.getBusinessData(date.atStartOfDay(), endOfDay(date));
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(date.toString());
                row.createCell(1).setCellValue(data.getTurnover() == null ? 0 : data.getTurnover().doubleValue());
                row.createCell(2).setCellValue(data.getValidOrderCount() == null ? 0 : data.getValidOrderCount());
                row.createCell(3).setCellValue(data.getOrderCompletionRate() == null ? 0 : data.getOrderCompletionRate());
                row.createCell(4).setCellValue(data.getUnitPrice() == null ? 0 : data.getUnitPrice().doubleValue());
                row.createCell(5).setCellValue(data.getNewUsers() == null ? 0 : data.getNewUsers());
            }

            BusinessDataVO overview = workspaceService.getBusinessData(begin.atStartOfDay(), endOfDay(end));
            Row summary = sheet.createRow(rowNum + 1);
            summary.createCell(0).setCellValue("合计");
            summary.createCell(1).setCellValue(overview.getTurnover() == null ? 0 : overview.getTurnover().doubleValue());
            summary.createCell(2).setCellValue(overview.getValidOrderCount() == null ? 0 : overview.getValidOrderCount());
            summary.createCell(3).setCellValue(overview.getOrderCompletionRate() == null ? 0 : overview.getOrderCompletionRate());
            summary.createCell(4).setCellValue(overview.getUnitPrice() == null ? 0 : overview.getUnitPrice().doubleValue());
            summary.createCell(5).setCellValue(overview.getNewUsers() == null ? 0 : overview.getNewUsers());

            String fileName = URLEncoder.encode("运营数据报表.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("导出运营数据失败", e);
            throw new BusinessException("导出运营数据失败");
        }
    }

    private List<LocalDate> dateList(LocalDate begin, LocalDate end) {
        if (begin == null || end == null || begin.isAfter(end)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dates.add(date);
        }
        return dates;
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return LocalDateTime.of(date.plusDays(1), LocalTime.MIN);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
