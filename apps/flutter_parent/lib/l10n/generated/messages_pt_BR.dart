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

  static m12(score, pointsPossible) => "${score} de ${pointsPossible} pontos";

  static m13(studentShortName) => "para ${studentShortName}";

  static m14(threshold) => "Nota do curso acima ${threshold}";

  static m15(threshold) => "Nota do curso abaixo ${threshold}";

  static m16(date, time) => "${date} às ${time}";

  static m17(canvasGuides, canvasSupport) => "Tente pesquisar o nome da escola ou distrito que você está tentando acessar, como “Smith Private School” ou “Smith County Schools.” Também é possível inserir um domínio do Canvas diretamente, como “smith.instructure.com.”\n\nPara mais informações sobre encontrar a conta do Canvas da sua instituição, você pode visitar o ${canvasGuides}, falar com o ${canvasSupport} ou contatar a sua escola para assistência.";

  static m18(date, time) => "Para ser entregue em ${date} às ${time}";

  static m19(userName) => "Você parará de agir como ${userName} e será desconectado.";

  static m20(userName) => "Você parará de agir como ${userName} e voltará à sua conta original.";

  static m21(studentName, eventTitle) => "Sobre: ${studentName}, Evento - ${eventTitle}";

  static m22(startAt, endAt) => "${startAt} - ${endAt}";

  static m23(grade) => "Nota final: ${grade}";

  static m24(studentName) => "Sobre: ${studentName}, Primeira página";

  static m25(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m26(studentName) => "Sobre: ${studentName}, Notas";

  static m27(pointsLost) => "Penalidade por atraso (-${pointsLost})";

  static m28(studentName, linkUrl) => "Sobre: ${studentName}, ${linkUrl}";

  static m29(percentage) => "Deve ser acima de ${percentage}";

  static m30(percentage) => "Deve ser abaixo de ${percentage}";

  static m31(month) => "Próximo mês: ${month}";

  static m32(date) => "Próxima semana começando ${date}";

  static m33(query) => "Não é possível encontrar escolas correspondendo a \"${query}\"";

  static m34(points, howMany) => "${Intl.plural(howMany, one: 'De 1 ponto', other: 'De ${points} pontos')}";

  static m35(count) => "+${count}";

  static m36(points) => "${points} pontos possíveis";

  static m37(month) => "Mês anterior: ${month}";

  static m38(date) => "Semana anterior começando ${date}";

  static m39(month) => "Mês de ${month}";

  static m40(date, time) => "Esta tarefa foi enviada em ${date} às ${time} e está esperando avaliação";

  static m41(studentName) => "Sobre: ${studentName}, Programa de estudos";

  static m42(count) => "${count} não lido";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Agir como\" é essencialmente fazer logon como este usuário sem uma senha. Você poderá realizar qualquer ação como se você fosse este usuário, e no ponto de vista dos outros usuários, será como se este usuário a estivesse executando. Contudo, logs de auditoria registram o fato de que você era a pessoa que realizou as ações em nome desse usuário."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Uma descrição é necessária."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Um assunto é necessário."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Agir como usuário"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Adicionar Estudante"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Adicionar anexo"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Adicionar novo aluno"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Adicionar aluno com..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Configurações do alerta"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Avisem-me quando..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Todos os períodos de avaliação"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Um endereço de e-mail é necessário."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Ocorreu um erro ao tentar exibir este link"),
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
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendários"),
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
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Modo escuro"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Excluir"),
    "Description" : MessageLookupByLibrary.simpleMessage("Descrição"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositivo"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modelo do dispositivo"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domínio"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domínio:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Feito"),
    "Download" : MessageLookupByLibrary.simpleMessage("Baixar"),
    "Due" : MessageLookupByLibrary.simpleMessage("Vencimento"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EMERGÊNCIA CRÍTICA EXTREMA!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Endereço de e-mail"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-mail:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Insira o código de emparelhamento do aluno fornecido. Se o código de emparelhamento não funcionar, ele pode ter expirado"),
    "Event" : MessageLookupByLibrary.simpleMessage("Evento"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Dispensado"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Falhou. Toque para opções."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtrar"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrar por"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Primeira página"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Mensagem de erro completa"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Nota"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Porcentagem da nota"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Avaliado"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Notas"),
    "Help" : MessageLookupByLibrary.simpleMessage("Ajuda"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Modo de alto contraste"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Como isso está afetando você?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Eu não posso fazer as coisas até que eu receba uma resposta de vocês."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Preciso de ajuda, mas não é urgente."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Estou tendo problemas em fazer login"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Ideia para o Aplicativo Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Caixa de entrada"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Caixa de Entrada Zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incompleto"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Aviso da instituição"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Avisos da Instituição"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruções"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interações nesta página são limitadas pela sua instituição."),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Parece um bom dia para descansar, relaxar e recarregar."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Parece que tarefas ainda não foram criadas nesse espaço."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Só uma pergunta casual, comentário, idéia, sugestão ..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Atrasado"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Executar ferramenta externa"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Modo claro"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Erro de link"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Localidade:"),
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
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Abrir em Navegador"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Abrir com outro app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Código de emparelhamento"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Preparando..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Logins prévios"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Política de privacidade"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Política de privacidade, termos e uso, fonte aberta"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Código QR"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Recipientes"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Lembrar-me"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Lembretes"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Responder"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Responder a todos"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Relatar um problema"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Solicitar ajuda com login"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Solicitar botão de ajuda com login"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Reiniciar app"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Tentar novamente"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Voltar ao login"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ALUNO"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Selecionar recipientes"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Enviar uma mensagem sobre esta tarefa"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Enviar uma mensagem sobre este curso"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Enviar mensagem"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Definir uma data e hora para ser notificado sobre este evento."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Definir uma data e hora para ser notificado sobre esta tarefa específica."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Configurações"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Divulgar o seu amor pelo aplicativo"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Alguma coisa não está funcionando, mas eu posso trabalhar em torno dela para conseguir fazer o que precisa ser feito."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Parar de Agir como Usuário"),
    "Student" : MessageLookupByLibrary.simpleMessage("Aluno"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Assunto"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Enviado"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Enviado com sucesso!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Resumo"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Alternar Usuários"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Programa"),
    "TA" : MessageLookupByLibrary.simpleMessage("Assistente"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("PROFESSOR"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Toque para favoritar os cursos que você deseja ver no calendário."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Toque para emparelhar com um novo aluno"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Toque para selecionar este aluno"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Toque para exibir o seletor de alunos"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Professor"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Conte-nos as suas partes favoritas do aplicativo"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Termos de Uso"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("As informações a seguir nos ajudarão a compreender melhor a sua ideia:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("O servidor que você digitou não está autorizado para este aplicativo."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("O agente de usuário para este aplicativo não é autorizado."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Não há aplicativos instalados que podem abrir este arquivo"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Não há informação da página disponível."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Houve um problema ao carregar os Termos de Uso"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar recipientes para este curso"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar os detalhes do resumo para este curso."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar este anúncio"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar esta conversa"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar este arquivo"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar as suas mensagens da caixa de entrada."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar os alertas do seu aluno."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar o calendário do seu aluno"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar seus alunos."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar os cursos do seu aluno."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Houve um erro ao tentar agir como este usuário. Verifique o Domínio e a ID de usuário e tente novamente."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Ainda não há nada para ser notificado."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Este aplicativo não é autorizado para uso."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Este curso ainda não tem quaisquer tarefas ou eventos de calendário."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Este arquivo não é suportado e não pode ser visto através do app"),
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
    "User ID" : MessageLookupByLibrary.simpleMessage("ID de Usuário"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID de Usuário:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Número da versão"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Visualizar detalhes do erro"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Atualmente estamos construindo este recurso para a sua visualização."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Não podemos exibir este link, ele pode pertencer a uma instituição à qual você não está logado atualmente."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Não conseguimos encontrar quaisquer alunos associados a esta conta"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Não fomos capazes de verificar o servidor para uso com este aplicativo."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Não temos certeza do que aconteceu, mas não foi bom. Contate-nos se isso continuar acontecendo."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sim"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Você não está observando quaisquer alunos."),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("É necessário inserir uma id de usuário"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("É necessário inserir um domínio válido"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Você será notificado sobre esta tarefa em..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Você será notificado sobre este evento em..."),
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
    "collapse" : MessageLookupByLibrary.simpleMessage("recolher"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("recolhido"),
    "contentDescriptionScoreOutOfPointsPossible" : m12,
    "courseForWhom" : m13,
    "courseGradeAboveThreshold" : m14,
    "courseGradeBelowThreshold" : m15,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cursos"),
    "dateAtTime" : m16,
    "domainSearchHelpBody" : m17,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Como encontrar minha escola ou distrito?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Inserir nome ou distrito da escola..."),
    "dueDateAtTime" : m18,
    "endMasqueradeLogoutMessage" : m19,
    "endMasqueradeMessage" : m20,
    "eventSubjectMessage" : m21,
    "eventTime" : m22,
    "expand" : MessageLookupByLibrary.simpleMessage("expandir"),
    "expanded" : MessageLookupByLibrary.simpleMessage("expandido"),
    "finalGrade" : m23,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Encontrar escola"),
    "frontPageSubjectMessage" : m24,
    "gradeFormatScoreOutOfPointsPossible" : m25,
    "gradesSubjectMessage" : m26,
    "latePenalty" : m27,
    "me" : MessageLookupByLibrary.simpleMessage("eu"),
    "messageLinkPostscript" : m28,
    "minus" : MessageLookupByLibrary.simpleMessage("menos"),
    "mustBeAboveN" : m29,
    "mustBeBelowN" : m30,
    "next" : MessageLookupByLibrary.simpleMessage("Próximo"),
    "nextMonth" : m31,
    "nextWeek" : m32,
    "noDomainResults" : m33,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m34,
    "plusRecipientCount" : m35,
    "pointsPossible" : m36,
    "previousMonth" : m37,
    "previousWeek" : m38,
    "selectedMonthLabel" : m39,
    "send" : MessageLookupByLibrary.simpleMessage("enviar"),
    "submissionStatusSuccessSubtitle" : m40,
    "syllabusSubjectMessage" : m41,
    "unread" : MessageLookupByLibrary.simpleMessage("não lido"),
    "unreadCount" : m42
  };
}
