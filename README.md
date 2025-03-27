# lab67_tcp-ip-rmi_JP
Lab6-7 - TCP/IP sockets + RMI

# Treść programowa

1. Rozpraszanie obliczeń poprzez wykorzystanie gniazd TCP/IP.
2. Rozpraszanie obliczeń poprzez zdalne wywoływanie procedur.

<aside>
💡 W celu wygospodarowania dodatkowych zajęć na ewentualne poprawy laboratoria 6 i 7 są połączone. Należy wykonać jedną aplikację (osobny klient i serwer) realizującą komunikację zarówno poprzez sockety TCP/IP jaki i zdalne wywoływanie procedur (”*Remote Method Invocation*” - RMI).

</aside>

# Cel zajęć

Celem zajęć jest implementacja gry “kółko i krzyżyk” (”*tick-tack-toe*”). Gra powinna działać w architekturze klient-serwer.

# Projekt

### Kluczowe funkcjonalności

1. **Hybrydowa komunikacja sieciowa** - użycie zarówno komunikacji poprzez RMI jak i gniazd TCP/IP.
2. **Mechanika gry** - implementacja silnika zarządzającego mechaniką gry w kółko i krzyżyk

### Obie grupy

- Należy wykorzystać mechanizm RMI w celu nawiązania połączenia między dwoma graczami oraz obsługę mechaniki gry (np. parowanie graczy, inicjalizowanie stanu gry, wymiana ruchów, walidacja I/O).
- Mechanika gry powinna być zaimplementowana w aplikacji serwera.
- Należy przemyśleć argumenty pobierane przez aplikacje JAR serwer/klient.
- Serwer powinen obsługiwać więcej niż jednego gracza w tym samym momencie (np. poprzez realizację gier w dedykowanych pokojach lub tworzenie tokenów poszczególnych sesji wymienianych przez graczy).
- Serwer powinen obliczać statystyki graczy w danej sesji - ilość wygranych, remisów oraz porażek.
- Aplikacja serwera powinna logować w konsoli kluczowe dane (parowanie użytkowników, błędy, koniec gry, itp).

<aside>
💡
  
**Java NIO (ciekawostka)**

Zalecane jest skorzystanie z pakietu `java.nio` (*New I/O*) umożliwiającego tworzenie nieblokujących aplikacji wejścia/wyjścia.

W tradycyjnym modelu serwer nasłuchuje na określonym porcie i czeka na nadchodzące żądania. Kiedy pojawi się nowe zapytanie, serwer deleguje jego obsługę do wątku z wcześniej przygotowanej puli. Podejście to ma jednak pewne ograniczenia. Po pierwsze, liczba jednocześnie obsługiwanych klientów jest ograniczona do rozmiaru puli wątków. Po drugie, jeśli któryś z klientów ma wolne połączenie, wątek przypisany do tego klienta spędza większość czasu czekając na dane, zamiast obsługiwać innych użytkowników. To prowadzi do nieefektywnego wykorzystania zasobów serwera.

Nieblokujący serwer jest rozwiązaniem, które próbuje wyeliminować te problemy. W takim modelu jeden wątek może obsługiwać wiele zapytań jednocześnie. Umożliwia to zastosowanie nieblokującego wejścia/wyjścia (IO), które w Javie realizowane jest za pomocą klas z pakietu `java.nio`. Dzięki temu serwer może efektywnie zarządzać wieloma połączeniami, nawet jeśli jedno z nich jest wolniejsze.

</aside>

## Grupa A

Implementacja funkcjonalności chatu pomiędzy użytkownikami z wykorzystaniem protokołu TCP/IP.

- Gniazda powinny być otworzone pomiędzy grającymi użytkownikami.
- Serwer udostępnia informację o adresach IP użytkowników

![image](https://github.com/user-attachments/assets/786bdbfe-5fce-47f7-9523-93f77405df21)
