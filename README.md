# \# 🍽 Seatopia – Sistem de rezervări restaurante

# 

# Seatopia este o aplicație desktop dezvoltată în Java (JavaFX) care permite gestionarea rezervărilor la restaurante, oferind funcționalități atât pentru clienți, cât și pentru restaurante.

# 

# ---

# 

# \## 📌 Descrierea problemei abordate

# 

# În prezent, multe restaurante gestionează rezervările manual (telefonic, pe hârtie sau prin mesaje), ceea ce poate duce la:

# 

# \- suprapuneri de rezervări

# \- lipsa evidenței meselor disponibile

# \- dificultăți în comunicarea cu clienții

# \- pierderea informațiilor despre rezervări

# 

# Seatopia rezolvă această problemă printr-un sistem centralizat care:

# 

# \- permite clienților să facă rezervări online

# \- permite restaurantelor să gestioneze mesele și rezervările

# \- trimite notificări automate prin email

# 

# ---

# 

# \## 🎯 Funcționalități implementate (Use Cases)

# 

# \### 👤 Client

# 

# \- Creare cont client

# \- Autentificare

# \- Vizualizare restaurante disponibile

# \- Creare rezervare (dată, oră, număr persoane)

# \- Vizualizare rezervările proprii

# \- Anulare rezervare

# \- Primire email de confirmare a rezervării

# 

# \### 🏬 Restaurant

# 

# \- Creare cont restaurant

# \- Autentificare

# \- Adăugare mese (nume, capacitate)

# \- Modificare și ștergere mese

# \- Vizualizare rezervări pe zile

# \- Confirmare rezervări

# \- Respingere rezervări

# \- Marcare rezervare ca COMPLETED sau NO\_SHOW

# \- Vizualizare rating client înainte de acceptare

# 

# ---

# 

# \## 🖥 Ecrane principale

# 

# \- Login

# \- Register Client

# \- Register Restaurant

# \- Dashboard Client

# \- Dashboard Restaurant

# \- Rezervările mele

# 

# ---

# 

# \## 🏗 Arhitectura aplicației

# 

# Aplicația folosește arhitectura pe straturi (Layered Architecture):

# 

# \### 🔹 UI Layer

# \- clase JavaFX (LoginView, ClientView, RestaurantView etc.)

# 

# \### 🔹 Service Layer

# \- AuthService

# \- ReservationService

# \- EmailService  

# 

# Conține logica aplicației.

# 

# \### 🔹 Repository Layer

# \- ClientRepo

# \- RestaurantRepo

# \- TableRepo

# \- ReservationRepo  

# 

# Acces la baza de date SQLite.

# 

# \### 🔹 Model Layer

# \- Client

# \- Restaurant

# \- DiningTable

# \- Reservation

# \- Enumeration: ReservationStatus

# 

# ---

# 

# \## 📊 Diagrama de clase

# 

# (diagrama UML care arată relațiile dintre Client, Restaurant, Reservation, DiningTable etc.)

# 

# ---

# 

# \## 🗄 Baza de date

# 

# Baza de date este realizată în SQLite și conține următoarele tabele:

# 

# \- users

# \- clients

# \- restaurants

# \- tables

# \- reservations





## \## 🧩 Relații între entități

## 

## | Entitate 1 | Cardinalitate | Entitate 2 |

## |-----------|--------------|-----------|

## | USERS | 1 — 1 | CLIENTS |

## | USERS | 1 — 1 | RESTAURANTS |

## | RESTAURANTS | 1 — \* | TABLES |

## | RESTAURANTS | 1 — \* | RESERVATIONS |

## | CLIENTS | 1 — \* | RESERVATIONS |

## | TABLES | 1 — \* | RESERVATIONS |



# ---

# 

# \## ✉ API extern

# 

# Aplicația integrează un serviciu de email (Resend API) pentru:

# 

# \- email de bun venit la creare cont

# \- email de confirmare rezervare

# 

# Protocol folosit: HTTPS (REST API)

# 

# ---

# 

# \## 🧪 Testare

# 

# Proiectul conține teste unitare realizate cu JUnit pentru:

# 

# \- validări

# \- logica de rezervare

# \- servicii principale

# 

# ---

# 

# \## 🛠 Tehnologii folosite

# 

# \- Java 25

# \- JavaFX

# \- SQLite

# \- Maven

# \- JUnit

# \- Resend Email API

# 

# ---

# 

# \## 👩‍💻 Autor

# 

# Mădălina Todea



