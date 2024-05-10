Navodila za uporabo:
______________________________________________________________________________________________________________________________________
najprej zazenemo ChatServer in poljubno stevilo clientov

ko zazenemo ChatClient file se izpise 'Enter your username:', kamor vpisemo uporabnisko ime.
Ce je uporabnisko ime ze v uporabi, nam server sam dodeli novo neuporabljeno ime.
Ce ne dobimo nobene napake to pomeni, da je bil username uspesno poslan na server in tam shranjen.

nato lahko preprosto v terminal vpisemo vsebino sporocila, ki ga zelimo poslati in pritisnemo Enter.
izpise se 'Enter 1 for 'public' && 2 for 'private':'
______________________________________________________________________________________________________________________________________
1. Public message
-> ce v terminal vpisemo '1' in pritisnemo enter bo sporocilo poslano vsem clientom povezanih na server.

2. Private message
-> ce v terminal vpisemo '2' in pritisnemo enter se izpise: 'Enter recipient username:',
   kamor vpisemo ime prejemnika, kateremu zelimo poslati sporocilo in pritisnemo Enter.

ce user  ni povezan na server, bo program izpisal 'Message could not be sent. User 'username' does not exist.',
message ponovno vpisemo, nastavimo na 'public' in vpisemo pravi username.   
______________________________________________________________________________________________________________________________________
-> ce ne dobimo nobene napake, je bilo sporocilo uspesno poslano in v terminal lahko vnesemo naslednje sporocilo.

[Da ugasnemo client pritisnemo ctrl + c]