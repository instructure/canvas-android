// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a pt_BR locale. All the
// messages from the main program should be duplicated here with the same
// function name.

// Ignore issues from commonly used lints in this file.
// ignore_for_file:unnecessary_brace_in_string_interps, unnecessary_new
// ignore_for_file:prefer_single_quotes,comment_references, directives_ordering
// ignore_for_file:annotate_overrides,prefer_generic_function_type_aliases
// ignore_for_file:unused_import, file_names

import 'package:intl/intl.dart';
import 'package:intl/message_lookup_by_library.dart';

final messages = new MessageLookup();

typedef String MessageIfAbsent(String messageStr, List<dynamic> args);

class MessageLookup extends MessageLookupByLibrary {
  String get localeName => 'pt_BR';

  static m0(userName) => "Você está agindo como ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Nota da tarefa acima ${threshold}";

  static m3(threshold) => "Nota da tarefa abaixo ${threshold}";

  static m4(moduleName) => "Esta tarefa está bloqueada pelo módulo \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Sobre: ${studentName}, Tarefa - ${assignmentName}";

  static m6(points) => "${points} pts";

  static m7(points) => "${points} pontos";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} para 1 outro', other: '${authorName} para ${howMany} outros')}";

  static m9(authorName, recipientName) => "${authorName} a ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} para ${recipientName} e 1 outro', other: '${authorName} para ${recipientName} e ${howMany} outros')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Alterar cor para ${studentName}";

  static m13(score, pointsPossible) => "${score} de ${pointsPossible} pontos";

  static m14(studentShortName) => "para ${studentShortName}";

  static m15(threshold) => "Nota do curso acima ${threshold}";

  static m16(threshold) => "Nota do curso abaixo ${threshold}";

  static m17(date, time) => "${date} às ${time}";

  static m18(alertTitle) => "Descartar ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Tente pesquisar o nome da escola ou distrito que você está tentando acessar, como “Smith Private School” ou “Smith County Schools.” Também é possível inserir um domínio do Canvas diretamente, como “smith.instructure.com.”\n\nPara mais informações sobre encontrar a conta do Canvas da sua instituição, você pode visitar o ${canvasGuides}, falar com o ${canvasSupport} ou contatar a sua escola para assistência.";

  static m20(date, time) => "Para ser entregue em ${date} às ${time}";

  static m21(userName) => "Você parará de agir como ${userName} e será desconectado.";

  static m22(userName) => "Você parará de agir como ${userName} e voltará à sua conta original.";

  static m23(studentName, eventTitle) => "Sobre: ${studentName}, Evento - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Nota final: ${grade}";

  static m26(studentName) => "Sobre: ${studentName}, Primeira página";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Sobre: ${studentName}, Notas";

  static m29(pointsLost) => "Penalidade por atraso (-${pointsLost})";

  static m30(studentName, linkUrl) => "Sobre: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Deve ser acima de ${percentage}";

  static m32(percentage) => "Deve ser abaixo de ${percentage}";

  static m33(month) => "Próximo mês: ${month}";

  static m34(date) => "Próxima semana começando ${date}";

  static m35(query) => "Não é possível encontrar escolas correspondendo a \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'De 1 ponto', other: 'De ${points} pontos')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} pontos possíveis";

  static m39(month) => "Mês anterior: ${month}";

  static m40(date) => "Semana anterior começando ${date}";

  static m41(termsOfService, privacyPolicy) => "Ao tocar em ‘Criar conta’, você concorda com os ${termsOfService} e reconhece a ${privacyPolicy}";

  static m42(version) => "Sugsetões para Android - Canvas Parent ${version}";

  static m43(month) => "Mês de ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} estrela', other: '${position} estrelas')}";

  static m45(date, time) => "Esta tarefa foi enviada em ${date} às ${time} e está esperando avaliação";

  static m46(studentName) => "Sobre: ${studentName}, Programa de estudos";

  static m47(count) => "${count} não lido";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Agir como\" é essencialmente fazer logon como este usuário sem uma senha. Você poderá realizar qualquer ação como se você fosse este usuário, e no ponto de vista dos outros usuários, será como se este usuário a estivesse executando. Contudo, logs de auditoria registram o fato de que você era a pessoa que realizou as ações em nome desse usuário."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Uma descrição é necessária."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Ocorreu um erro de rede ao adicionar este aluno. Verifique sua conexão e tente novamente."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Um assunto é necessário."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Agir como usuário"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Adicionar Estudante"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Adicionar anexo"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Adicionar novo aluno"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Adicionar aluno com..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Configurações do alerta"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Avisem-me quando..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Todos os períodos de avaliação"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Já tem uma conta? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Um endereço de e-mail é necessário."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Ocorreu um erro ao tentar exibir este link"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Um erro ocorreu ao salvar a sua seleção. Por favor, tente novamente."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Ocorreu um erro inesperado"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Versão SO do Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Aparência"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Versão do aplicativo"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Você é um aluno ou professor?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Tem certeza de que deseja sair?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Tem certeza de que deseja fechar esta página? Suas mensagens não enviadas serão perdidas."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Detalhes da tarefa"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Nota da tarefa acima"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Nota da tarefa abaixo"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Tarefa em falta"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, Fuschia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendários"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Permissão para câmera"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Cancelar"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas no GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Escolher um curso para enviar mensagem"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Escolher na galeria"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Concluído"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Entre em contato com o suporte"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Aviso do curso"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Avisos do Curso"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Nota do curso acima"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Nota do curso abaixo"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Criar conta"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Modo escuro"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Excluir"),
    "Description" : MessageLookupByLibrary.simpleMessage("Descrição"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositivo"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modelo do dispositivo"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domínio"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domínio:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Não mostrar de novo"),
    "Done" : MessageLookupByLibrary.simpleMessage("Feito"),
    "Download" : MessageLookupByLibrary.simpleMessage("Baixar"),
    "Due" : MessageLookupByLibrary.simpleMessage("Vencimento"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EMERGÊNCIA CRÍTICA EXTREMA!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elétrica, azul"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Endereço de e-mail"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-mail:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("E-mail..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Insira o código de emparelhamento do aluno fornecido. Se o código de emparelhamento não funcionar, ele pode ter expirado"),
    "Event" : MessageLookupByLibrary.simpleMessage("Evento"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Dispensado"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Código QR expirado"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Falhou. Toque para opções."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtrar"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrar por"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Fogo, laranja"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Primeira página"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Nome completo"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Nome completo..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Mensagem de erro completa"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Ir para hoje"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Avaliar"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Porcentagem da nota"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Avaliado"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Notas"),
    "Help" : MessageLookupByLibrary.simpleMessage("Ajuda"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Ocultar senha"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Modo de alto contraste"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Como estamos indo?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Como isso está afetando você?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Eu não posso fazer as coisas até que eu receba uma resposta de vocês."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Eu não tenho uma conta do Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Eu tenho uma conta do Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Preciso de ajuda, mas não é urgente."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Estou tendo problemas em fazer login"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Ideia para o Aplicativo Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Para fornecer uma melhor experiência, atualizamos como os lembretes funcionam. É possível adicionar novos lembretes ao visualizar uma tarefa ou evento de calendário e tocar no interruptor sob a seção \"Lembrar de mim\".\n\nEsteja ciente de que quaisquer lembretes criados com versões antigas deste app não serão compatíveis com as novas alterações e será necessário criá-los novamente."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Caixa de entrada"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Caixa de Entrada Zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incompleto"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Domínio incorreto"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Aviso da instituição"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Avisos da Instituição"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruções"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interações nesta página são limitadas pela sua instituição."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Código QR inválido"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Parece um bom dia para descansar, relaxar e recarregar."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Parece que tarefas ainda não foram criadas nesse espaço."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Só uma pergunta casual, comentário, idéia, sugestão ..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Atrasado"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Executar ferramenta externa"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Modo claro"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Erro de link"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Localidade:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Localizar código QR"),
    "Location" : MessageLookupByLibrary.simpleMessage("Localização"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Bloqueado(a)"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Sair"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Fluxo de login: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Fluxo de login: Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Fluxo de login: Administrador do site"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Fluxo de login: Pular verificação móvel"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Gerenciar alunos"),
    "Message" : MessageLookupByLibrary.simpleMessage("Mensagem"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Assunto da mensagem"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Faltante"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Deve ser abaixo de 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Erro de rede"),
    "Never" : MessageLookupByLibrary.simpleMessage("Nunca"),
    "New message" : MessageLookupByLibrary.simpleMessage("Nova mensagem"),
    "No" : MessageLookupByLibrary.simpleMessage("Não"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Nenhum alerta"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Sem tarefas"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Sem Cursos"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Sem prazo de entrega"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Nenhum evento hoje!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Sem nota"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Nenhum Local Especificado"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Sem Alunos"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Sem assunto"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Sem resumo"),
    "No description" : MessageLookupByLibrary.simpleMessage("Sem descrição"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Nenhum recipiente selecionado"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Sem nota"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Não Enviado"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Não é um pai?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Notificações para lembretes sobre tarefas e eventos do calendário"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Versão OS"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observador"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Um dos nossos outros aplicativos pode ser uma escolha melhor. Toque em um para visitar o Play Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Abrir Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Abrir em Navegador"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Abrir com outro app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Código de emparelhamento"),
    "Password" : MessageLookupByLibrary.simpleMessage("Senha"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Senha é necessária"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Senha deve ter pelo menos 8 caracteres"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Senha..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Nota do planejador"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Insira um endereço de e-mail válido"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Insira um endereço de e-mail"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Insira nome completo"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Escaneie um código QR gerado pelo Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Ameixa, roxo"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Preparando..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Logins prévios"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Política de privacidade"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Link da Política de Privacidade"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Política de privacidade, termos e uso, fonte aberta"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Código QR"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("O escaneamento QR requer acesso à câmera"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Framboesa, vermelho"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Recipientes"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Atualizar"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Lembrar-me"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Lembretes"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Lembretes mudaram!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Responder"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Responder a todos"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Relatar um problema"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Solicitar ajuda com login"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Solicitar botão de ajuda com login"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Reiniciar app"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Tentar novamente"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Voltar ao login"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ALUNO"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Captura de tela mostrando local da geração do código QR no navegador"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Captura de tela mostrando o local da geração de código QR emparelhado no aplicativo Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("Selecionar"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Selecionar cor do aluno"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Selecionar recipientes"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Enviar Comentários"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Enviar uma mensagem sobre esta tarefa"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Enviar uma mensagem sobre este curso"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Enviar mensagem"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Definir uma data e hora para ser notificado sobre este evento."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Definir uma data e hora para ser notificado sobre esta tarefa específica."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Configurações"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Trevo, verde"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Divulgar o seu amor pelo aplicativo"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Exibir senha"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Conectar"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Algo deu errado ao tentar criar sua conta. Entre em contato com sua escola para obter ajuda."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Alguma coisa não está funcionando, mas eu posso trabalhar em torno dela para conseguir fazer o que precisa ser feito."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Parar de Agir como Usuário"),
    "Student" : MessageLookupByLibrary.simpleMessage("Aluno"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Emparelhamento do aluno"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Os alunos podem criar um código QR usando o aplicativo Canvas Student em seu dispositivo móvel"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Os alunos podem obter um código de emparelhamento no site do Canvas"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Assunto"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Enviado"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Enviado com sucesso!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Resumo"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Alternar Usuários"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Programa"),
    "TA" : MessageLookupByLibrary.simpleMessage("Assistente"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("PROFESSOR"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Toque para favoritar os cursos que você deseja ver no calendário. Selecione até 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Toque para emparelhar com um novo aluno"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Toque para selecionar este aluno"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Toque para exibir o seletor de alunos"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Professor"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Conte-nos as suas partes favoritas do aplicativo"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Termos de Serviço"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Link dos Termos de Serviço"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Termos de Uso"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("O código QR que você escaneou pode ter expirado. Atualize o código no dispositivo do aluno e tente novamente."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("As informações a seguir nos ajudarão a compreender melhor a sua ideia:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("O servidor que você digitou não está autorizado para este aplicativo."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("O aluno que você está tentando adicionar pertence a uma escola diferente. Faça login ou crie uma conta nessa escola para escanear esse código."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("O agente de usuário para este aplicativo não é autorizado."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Não há aplicativos instalados que podem abrir este arquivo"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Não há informação da página disponível."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Houve um problema ao carregar os Termos de Uso"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Ocorreu um problema ao remover este aluno da sua conta. Verifique a sua conexão e tente novamente."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar recipientes para este curso"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar os detalhes do resumo para este curso."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar este anúncio"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar esta conversa"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar este arquivo"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar as suas mensagens da caixa de entrada."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar os alertas do seu aluno."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar o calendário do seu aluno"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar seus alunos."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Ocorreu um erro ao carregar os cursos dos seus alunos."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Houve um erro ao fazer login. Gere outro código QR e tente novamente."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Houve um erro ao tentar agir como este usuário. Verifique o Domínio e a ID de usuário e tente novamente."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Ainda não há nada para ser notificado."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Este aplicativo não é autorizado para uso."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Este curso ainda não tem quaisquer tarefas ou eventos de calendário."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Este arquivo não é suportado e não pode ser visto através do app"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Isso desemparelhará e removerá todas as matrículas desse aluno da sua conta."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Nota Total"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Que difícil..."),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Não é possível obter cursos. Verifique a sua conexão e tente novamente."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Não é possível carregar esta mensagem"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Não é possível reproduzir este arquivo de mídia"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Incapaz de enviar mensagem. Verifique sua conexão e tente novamente."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Em construção"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Usuário desconhecido"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Alterações não salvas"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Arquivo não suportado"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Carregar arquivo"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Usar câmera"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Usar tema escuro em conteúdo web"),
    "User ID" : MessageLookupByLibrary.simpleMessage("ID de Usuário"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID de Usuário:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Número da versão"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Ver descrição"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Visualizar detalhes do erro"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Visualizar Política de Privacidade"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Atualmente estamos construindo este recurso para a sua visualização."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Não podemos exibir este link, ele pode pertencer a uma instituição à qual você não está logado atualmente."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Não conseguimos encontrar quaisquer alunos associados a esta conta"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Não fomos capazes de verificar o servidor para uso com este aplicativo."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Não temos certeza do que aconteceu, mas não foi bom. Contate-nos se isso continuar acontecendo."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("O que podemos fazer melhor?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sim"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Você não está observando quaisquer alunos."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Você pode escolher apenas 10 calendários para exibir"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("É necessário inserir uma id de usuário"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("É necessário inserir um domínio válido"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Você deve selecionar pelo menos um calendário para exibir"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Você será notificado sobre esta tarefa em..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Você será notificado sobre este evento em..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Você encontrará o código QR na web no seu perfil da conta. Clique em ‘QR para login móvel’ na lista."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Você precisará abrir o aplicativo Canvas Student do aluno para continuar. Vá para o Menu Principal > Configurações > Emparelhar com o Observer e escaneie o código QR que você vê lá."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Seu código está incorreto ou expirado."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Os cursos do seu aluno podem ainda não ter sido publicados."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Você está em dia!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Alertas"),
    "appVersion" : m1,
    "assignmentGradeAboveThreshold" : m2,
    "assignmentGradeBelowThreshold" : m3,
    "assignmentLockedModule" : m4,
    "assignmentSubjectMessage" : m5,
    "assignmentTotalPoints" : m6,
    "assignmentTotalPointsAccessible" : m7,
    "authorToNOthers" : m8,
    "authorToRecipient" : m9,
    "authorToRecipientAndNOthers" : m10,
    "badgeNumberPlus" : m11,
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendário"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas Guides"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logotipo do Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Suporte do Canvas"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("recolher"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("recolhido"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cursos"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Como encontrar minha escola ou distrito?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Inserir nome ou distrito da escola..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("expandir"),
    "expanded" : MessageLookupByLibrary.simpleMessage("expandido"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Encontrar escola"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("eu"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("menos"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Próximo"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("enviar"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("não lido"),
    "unreadCount" : m47
  };
}
