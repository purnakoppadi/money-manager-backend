package com.purnakoppadi.moneymanager.service;


import com.purnakoppadi.moneymanager.dto.ExpenseDTO;
import com.purnakoppadi.moneymanager.entity.ProfileEntity;
import com.purnakoppadi.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money-manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 10 * * *",zone = "Asia/Kolkata")
    public void sendDailyIncomeExpenseRemainder()
    {
        log.info("Job started: sendDailyIncomeExpenseRemainder()");
        List<ProfileEntity> profiles=profileRepository.findAll();
        for(ProfileEntity profile: profiles)
        {
            String body = "Hi " + profile.getFullName() + ",<br><br>"
                    + "This is a friendly reminder to add your income and expenses for today in Money Manager."
                    + "<br><br>"
                    + "<a href=\"" + frontendUrl + "\" "
                    + "style='display:inline-block;padding:10px 20px;background-color:#007bff;color:white;text-decoration:none;border-radius:5px;'>"
                    + "Open Money Manager"
                    + "</a>"
                    + "<br><br>Best regards,<br>Money Manager Team";

            emailService.sendEmail(profile.getEmail(),"Daily reminder: Add your income and expenses",body);
        }
        log.info("Job completed : sendDailyIncomeExpenseReminder()");
    }



//    @Scheduled(cron = "0 0 23 * * *",zone = "IST")
@Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kolkata")
public void sendDailyExpenseSummary() {

    log.info("Job started: sendDailyExpenseSummary()");

    List<ProfileEntity> profiles = profileRepository.findAll();

    for (ProfileEntity profile : profiles) {

        List<ExpenseDTO> todaysExpenses =
                expenseService.getExpensesForUserOnDate(
                        profile.getId(),
                        LocalDate.now(ZoneId.of("Asia/Kolkata"))
                );

        if (!todaysExpenses.isEmpty()) {

            StringBuilder table = new StringBuilder();

            table.append("<table style='border-collapse:collapse;width:100%;'>");

            table.append("<tr style='background-color:#f2f2f2;'>"
                    + "<th style='border:1px solid #ddd;padding:8px;'>S.No</th>"
                    + "<th style='border:1px solid #ddd;padding:8px;'>Name</th>"
                    + "<th style='border:1px solid #ddd;padding:8px;'>Amount</th>"
                    + "<th style='border:1px solid #ddd;padding:8px;'>Category</th>"
                    + "<th style='border:1px solid #ddd;padding:8px;'>Date</th>"
                    + "</tr>");

            int i = 1;

            for (ExpenseDTO expenseDTO : todaysExpenses) {

                table.append("<tr>");

                table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                        .append(i++)
                        .append("</td>");

                table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                        .append(expenseDTO.getName())
                        .append("</td>");

                table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                        .append(expenseDTO.getAmount())
                        .append("</td>");

                table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                        .append(expenseDTO.getCategoryName())
                        .append("</td>");

                table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                        .append(expenseDTO.getDate())
                        .append("</td>");

                table.append("</tr>");
            }

            table.append("</table>");

            String body =
                    "Hi " + profile.getFullName() + ",<br/><br/>"
                            + "Here is a summary of your expenses for today:"
                            + "<br/><br/>"
                            + table
                            + "<br/><br/>"
                            + "Best regards,<br/>"
                            + "Money Manager Team";

            emailService.sendEmail(
                    profile.getEmail(),
                    "Your Daily Expense Summary",
                    body
            );
        }
    }

    log.info("Job completed: sendDailyExpenseSummary()");
}
}
