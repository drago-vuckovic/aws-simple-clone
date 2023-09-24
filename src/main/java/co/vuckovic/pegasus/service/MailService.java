package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.model.dto.Invitation;
import co.vuckovic.pegasus.model.dto.Tenant;
import co.vuckovic.pegasus.model.dto.User;
import co.vuckovic.pegasus.config.FrontendProperties;
import co.vuckovic.pegasus.config.MailProperties;
import co.vuckovic.pegasus.model.exception.HttpException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailService {

  private final JavaMailSender mailSender;
  private final MailContentBuilder mailContentBuilder;

  private final MailProperties mailProperties;

  private final FrontendProperties frontendProperties;

  public void sendConfirmationMail(User user, Tenant tenant) {
    try {
      String link =
          String.format(
              "%s/verify-email?id=%s&verificationCode=%s&company=%s",
              frontendProperties.getBaseUrl(), user.getId(), user.getVerificationCode(),
              tenant.getCompany());
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message);
      String content = mailContentBuilder.buildMailTemplate(link, "verifyMailTemplate");
      helper.setFrom(mailProperties.getUsername());
      helper.setSubject(mailProperties.getConfirmationSubject());
      helper.setTo(user.getEmail());
      helper.setText(content, true);
      mailSender.send(message);
    } catch (Exception e) {
      log.error(String.format(mailProperties.getLogErrorTemplate(), e.getMessage()));
      throw new HttpException(mailProperties.getErrorMessage());
    }
  }

  public void sendRecoveryMail(User user) {
    try {
      String link =
          String.format(
              "%s/reset-password?verificationCode=%s", frontendProperties.getBaseUrl(),
              user.getVerificationCode());
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message);
      String content = mailContentBuilder.buildMailTemplate(link, "recoverPasswordTemplate");
      helper.setFrom(mailProperties.getUsername());
      helper.setSubject(mailProperties.getRecoverySubject());
      helper.setTo(user.getEmail());
      helper.setText(content, true);
      mailSender.send(message);
    } catch (Exception e) {
      log.error(String.format(mailProperties.getLogErrorTemplate(), e.getMessage()));
      throw new HttpException(mailProperties.getErrorMessage());
    }
  }

  public void sendInvitationMail(Invitation invitation, Tenant tenant) {
    try {
      String link =
          String.format(
              "%s/set-account-password?invitationId=%s&verificationCode=%s&tenantId=%s",
              frontendProperties.getBaseUrl(), invitation.getId(),
              invitation.getVerificationCode(), invitation.getTenantId());
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message);
      String content = mailContentBuilder.buildInvitationTemplate(link, invitation.getFirstname(),
          invitation.getLastname(), tenant.getCompany(), "invitationMailTemplate");
      helper.setFrom(mailProperties.getUsername());
      helper.setSubject(mailProperties.getInvitationSubject());
      helper.setTo(invitation.getEmail());
      helper.setText(content, true);
      mailSender.send(message);
    } catch (Exception e) {
      log.error(String.format(mailProperties.getLogErrorTemplate(), e.getMessage()));
      throw new HttpException(mailProperties.getErrorMessage());
    }
  }
}
