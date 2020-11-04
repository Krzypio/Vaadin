# Stany
Projekt Java Web App z wykorzystaniem Maven, Spring Boot, Hibernate, Vaadin. Aktualnie server Tomcat i baza danych H2. Ostatecznie użyty będzie WildFly i MySql.

Cel: określenie możliwości realizacji planu produkcji przy wykorzystaniu posiadanych zasobów narzędzi, lista narzędzi do przeniesienia, .

Opis problemu:
Narzędzia są umieszczone w magazynie oraz na stanowiskach produkcyjnych. Jedno narzędzie może znajdować się na wielu stanowiskach.
Stanowiska nieaktywne, tj. nie ujęte w planie produkcyjnym są razem z magazynem potencjalnymi dawcami narzędzi. 
Stanowiska o wcześniejszej dacie produkcji mają wyższy priorytet.
Wynikiem programu ma być lista narzędzi do przeniesienia z nieaktywnych stanowisk i magazynu na stanowiska aktywne, oraz lista narzędzi brakujących.
