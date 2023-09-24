package co.vuckovic.pegasus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailContentBuilder {

  private final TemplateEngine templateEngine;

  public String buildMailTemplate(String link, String template) {
    Context context = new Context();
    context.setVariable("link", link);
    return templateEngine.process(template, context);
  }

  public String buildInvitationTemplate(String link, String firstname, String lastname,
      String company, String template) {
    Context context = new Context();
    context.setVariable("link", link);
    context.setVariable("firstname", firstname);
    context.setVariable("lastname", lastname);
    context.setVariable("company", company);
    return templateEngine.process(template, context);
  }
}
