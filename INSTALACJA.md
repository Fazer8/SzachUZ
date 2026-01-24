# Instrukcja włączania

## Pierwsze uruchomienie (w ścieżce do programu)

1. Ustawienie pliku zmiennych środowiskowych .env (wysłane archiwum posiada gotowy plik).
  ```
  PG_ADMIN_USER=
  PG_ADMIN_PASSWD=
  PG_DATA_PATH=./postgres_data
  #RECAPTCHA_SITE_KEY=
  RECAPTCHA_SECRET_KEY=
  ```
2. Uruchomienie dockera - ``docker compose up``
3. Kompilacja aplikacji przy użyciu maven oraz budowanie nowego kontenera z serwerem tomcat:

  |               Windows               |            Linux           |
  |:-----------------------------------:|:--------------------------:|
  | ``mvn clean package``               | ``bash ./web_restart.sh``  |
  | w powershell: ``.\web_restart.ps1`` |                            |
  |                                     |                            |

