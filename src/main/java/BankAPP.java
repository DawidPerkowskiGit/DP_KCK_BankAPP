import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.Serializable;
import java.nio.charset.Charset;



import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.*;
import com.googlecode.lanterna.gui.component.*;
import com.googlecode.lanterna.gui.layout.VerticalLayout;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalSize;

/**
 *
 */
class ZwroconeWartosci implements Serializable{
    /**
     * Kod opowiadający wynikowi operacji
     */
    private int kod;

    /**
     * Opis zwróconego kodu
     */
    private String wiadomosc;

    ZwroconeWartosci(int kod, String wiadomosc) {
        this.kod = kod;
        this.wiadomosc = wiadomosc;
    }

    public int getKod() {
        return kod;
    }

    public String getWiadomosc() {
        return wiadomosc;
    }

    public void setKod(int kod) {
        this.kod = kod;
    }

    public void setWiadomosc(String wiadomosc) {
        this.wiadomosc = wiadomosc;
    }
}

/**
 * Rachunek
 */
abstract class Rachunek implements Serializable {

    private String nazwaRachunku;
    private double saldo;

    /**
     * Lista przechowująca historię transakcji tego rachunku.
     */
    protected ArrayList<String> historiaTransakcji = new ArrayList<>();


    Rachunek() {}

    /**
     * Pobranie wartości salda
     */
    public double getSaldo() {
        return this.saldo;
    }

    /**
     * Pobranie nazwy konta
     */
    public String getNazwaRachunku() {
        return this.nazwaRachunku;
    }
    
    public ArrayList<String> getHistoriaTransakcji(){
        return this.historiaTransakcji;
    }

    public void setNazwaRachunku(String nazwaRachunku) {
        this.nazwaRachunku = nazwaRachunku;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public void dodajWpisHistoriiTransakcji(String wpis) {
        this.historiaTransakcji.add(wpis);
    }


    public ZwroconeWartosci czyMoznaZmniejszycSrodki(double kwota) {
        if (getSaldo() > kwota) {
            return new ZwroconeWartosci(1, "Wystarczająca ilość środków do przeprowadzenia operacji");
        }
        else {
            return new ZwroconeWartosci(0, "Niewystarczająca ilość środków do wykonania operacji");
        }
    }

    public ZwroconeWartosci czyMoznaDodacSrodki(double kwota) {
        if (kwota > 0) {
            return new ZwroconeWartosci(1, "Można zasilić rachunek kwotą: "+ kwota +" złotych");
        }
        else {
            return new ZwroconeWartosci(0, "Nie można zasilić rachunku kwotą :"+ kwota +" złotych");
        }
    }



    /**
     * Pobranie wartości prowizji przelewu
     */
    public double getKosztPrzelewu(double kwota) {
        return 0.00;
    }

    public double getKosztWyplaty() {
        return 0.00;
    }


    public ZwroconeWartosci uaktulanijStanRachunku (double kwota) {
        if (getSaldo() + kwota >= 0) {
            setSaldo(getSaldo() + kwota);
            return new ZwroconeWartosci(1, "Pomyślnie zaktualizowano saldo");
        }
        else {
            return new ZwroconeWartosci(0, "Nie można zaktualizować salda");
        }
    }

    /**
     * Wpłacenie gotówki na rachunek i wpisanie tej operacji do listy przechowującej historie transakcji.
     */
    public ZwroconeWartosci zwiekszSaldo(double iloscGotowki) {

        ZwroconeWartosci czyTransakcjaSiePowiedzie = czyMoznaDodacSrodki(iloscGotowki);

        if (czyTransakcjaSiePowiedzie.getKod() == 1) {
            uaktulanijStanRachunku(iloscGotowki);
            czyTransakcjaSiePowiedzie.setWiadomosc("Wpłata " + iloscGotowki + " | " + this.wypiszSaldo());

        }
        else {
            czyTransakcjaSiePowiedzie.setWiadomosc("Nie można wykonać operacji, podana kwota wpłaty gotówki wynosi " + iloscGotowki + " złotych");
        }

        return czyTransakcjaSiePowiedzie;
    }


    /**
     * Domyślanie wyplata pieniędzy z rachunku.
     */
    public ZwroconeWartosci zmniejszSaldo(double iloscGotowki) {

        double lacznaKwotaTransakcji = iloscGotowki + getKosztWyplaty();

        ZwroconeWartosci czyTransakcjaSiePowiedzie = czyMoznaZmniejszycSrodki(lacznaKwotaTransakcji);

        if (czyTransakcjaSiePowiedzie.getKod() == 1) {
            setSaldo(getSaldo()-iloscGotowki);
            czyTransakcjaSiePowiedzie.setWiadomosc("Wyplata " + iloscGotowki + " | " + this.wypiszSaldo());

        }
        else {
            czyTransakcjaSiePowiedzie.setWiadomosc("Nie można wypłacić gotówki, niewystarczająca ilość środków lub podana nieprawidłową wartość");
        }
        return czyTransakcjaSiePowiedzie;
    }



    public ZwroconeWartosci przelewPrzychodzacy(double kwota) {
        ZwroconeWartosci czyPrzelewSiePowiedzie = czyMoznaDodacSrodki(kwota);

        if (czyPrzelewSiePowiedzie.getKod() == 1) {
            czyPrzelewSiePowiedzie.setWiadomosc("przesłał Ci "+ kwota +" złotych.");
        }
        else {
            czyPrzelewSiePowiedzie.setWiadomosc("Nie można otrzymać przelewu");
        }
        return czyPrzelewSiePowiedzie;
    }

    public ZwroconeWartosci przelewWychodzacy(double kwota) {
        double calkowityKosztPrzelewu = kwota + getKosztPrzelewu(kwota);
        ZwroconeWartosci czyPrzelewSiePowiedzie = czyMoznaZmniejszycSrodki(calkowityKosztPrzelewu);

        if (czyPrzelewSiePowiedzie.getKod() == 1) {
            czyPrzelewSiePowiedzie.setWiadomosc("Przelano "+ kwota +" złotych.");
        }
        else {
            czyPrzelewSiePowiedzie.setWiadomosc("Nie można zlecić przelewu na "+ kwota +" złotych. Brak wystarczającej ilości środków na koncie");
        }
        return  czyPrzelewSiePowiedzie;
    }



    /**
     * Wyświetlenie stanu salda
     */
    public String wypiszSaldo() {
        return "Saldo : " + this.saldo;
    }
}


/**
 * Rachunek Zwykły
 */

class RachunekZwykly extends Rachunek implements Serializable {

    /**
     *Domyslnie kazde nowy zwykly rachunek bedzie mial nazwe "Zwykly rachunek"
     */
    RachunekZwykly() {
        this.setNazwaRachunku("Rachunek zwykły");
    }

    /**
     * Tworzenie nowego rachunku ale z podaniem wlasnej nazwy
     */
    RachunekZwykly(String nazwa) {
        this.setNazwaRachunku(nazwa);
    }

    @Override
    public double getKosztWyplaty() {
        return 1.50;
    }

    @Override
    public String toString() {
        return getNazwaRachunku() + " | " + this.wypiszSaldo();
    }
}

/**
 * Rachunek oszczędnościowy
 */


class RachunekOszczednosciowy extends Rachunek implements Serializable {

    /**
     * Koszt wykonania przelewu dla oszczędnościowego typu rachunku.
     */
    private final double kosztPrzelewu = 1.50;

    /**
     * Domyślnie stworzony rachunek oszczędnościowy ma nazwa "Rachunek oszczędnościowy"
     */
    RachunekOszczednosciowy() {
        setNazwaRachunku("Rachunek oszczędnościowy");
    }

    /**
     * Stworzenie rachunku o wskazanej nazwie
     */
    RachunekOszczednosciowy(String nazwa) {
        setNazwaRachunku(nazwa);
    }

    /**
     * Prowizja od wyplaty gotowki jest zalezna od wyplacanej kwoty
     */
    @Override
    public double getKosztPrzelewu(double wartoscTransakcji) {
        if (wartoscTransakcji < 20 && wartoscTransakcji > 0)
            return 1.50;
        else if (wartoscTransakcji < 100)
            return 5.00;
        else if (wartoscTransakcji < 1000)
            return 20.00;
        else if (wartoscTransakcji > 1000) {
            return wartoscTransakcji * 0.20;
        } else
            return -1;
    }

    @Override
    public double getKosztWyplaty() {
        return 5.00;
    }
}


/**
 * Klient
 */

class Klient implements Serializable {
    private String Imie, Nazwisko;

    private ArrayList<Rachunek> listaRachunkow = new ArrayList<>();

    Klient() {}

    /**
     * Stworzenie klienta o podanym imieniu i nazwisku, domyslnie kazdy klient otrzymuje zwykly rachunek
     */
    Klient(String Imie, String Nazwisko) {
        this.Imie = Imie;
        this.Nazwisko = Nazwisko;
        this.listaRachunkow.add(new RachunekZwykly("Zwykły rachunek #1"));
    }

    /**
     * Pobranie Imienia
     */
    public String getImie() {
        return Imie;
    }

    /**
     * Pobranie nazwiska
     */
    public String getNazwisko() {
        return Nazwisko;
    }

    public ArrayList<Rachunek> getListaRachunkow() {
        return this.listaRachunkow;
    }

    /**
     * Stworzenia zwyklego rachunku z domyslna nazwa oraz dodanie go do listy rachunkow tego klienta
     */
    public void dodajRachunekZwykly() {
        listaRachunkow.add(new RachunekZwykly());
    }

    /**
     * Stworzenia zwylego rachunku z wskazana nazwa oraz dodanie go do listy rachunkow tego klienta
     */
    public void dodajRachunekZwykly(String nazwaRachunku) {
        listaRachunkow.add(new RachunekZwykly(nazwaRachunku));
    }

    /**
     * Stworzenia rachunku oszczednosciowego z domyslna nazwa oraz dodanie go do listy rachunkow tego klienta
     */
    public void dodajRachunekOszczednosciowy() {
        listaRachunkow.add(new RachunekOszczednosciowy());
    }

    /**
     * Stworzenia rachunku oszczednosciowego z wskazana nazwa oraz dodanie go do listy rachunkow tego klienta
     */
    public void dodajRachunekOszczednosciowy(String nazwa) {
        listaRachunkow.add(new RachunekOszczednosciowy(nazwa));
    }

    /**
     * Usuniecie wskazanego rachunku, parametrem jest indeks na liscie
     */
    public void usunRachunek(int ktoryNaLiscie) {
        getListaRachunkow().remove(ktoryNaLiscie);
    }

    public ArrayList<ZwroconeWartosci> ustalPoprawnoscPrzelewu(double kwotaPrzelewu, int idRachunkuNadawcy, Klient klientOdbiorca, int idRachunkuOdbiorcy) {
        double kwotaCalkowita = kwotaPrzelewu + getListaRachunkow().get(idRachunkuNadawcy).getKosztPrzelewu(kwotaPrzelewu);

        ZwroconeWartosci czyPrzelewZlecajacegoSiePowiedzie = getListaRachunkow().get(idRachunkuNadawcy).przelewWychodzacy(kwotaCalkowita );
        ZwroconeWartosci czyPrzelewOdbiorcySiePowiedzie = klientOdbiorca.getListaRachunkow().get(idRachunkuOdbiorcy).przelewPrzychodzacy(kwotaPrzelewu);
        if (czyPrzelewZlecajacegoSiePowiedzie.getKod() == 1 && czyPrzelewOdbiorcySiePowiedzie.getKod() == 1) {
            czyPrzelewZlecajacegoSiePowiedzie.setWiadomosc( czyPrzelewZlecajacegoSiePowiedzie.getWiadomosc() + " Przelew otrzymał "+klientOdbiorca.getImie() +" "+getNazwisko() + ". ");
            czyPrzelewOdbiorcySiePowiedzie.setWiadomosc(getImie()+ " "+getNazwisko()+ " "+ czyPrzelewOdbiorcySiePowiedzie.getWiadomosc());
        }
        else {
            czyPrzelewOdbiorcySiePowiedzie.setKod(0);
            czyPrzelewZlecajacegoSiePowiedzie.setKod(0);
        }
        ArrayList<ZwroconeWartosci> zwroconaLista = new ArrayList<>();
        zwroconaLista.add(czyPrzelewZlecajacegoSiePowiedzie);
        zwroconaLista.add(czyPrzelewOdbiorcySiePowiedzie);
        return zwroconaLista;
    }
}



class ListaKlientow implements Serializable{

    private ArrayList<Klient> listaWszystkichKlientow = new ArrayList<>();

    public ArrayList<Klient> getListaWszystkichKlientow() {
        return this.listaWszystkichKlientow;
    }

    public void setListaWszystkichKlientow(ArrayList<Klient> lista) {
        this.listaWszystkichKlientow = lista;
    }

    public Klient pobierzKlienta(int idKlienta) {
        return getListaWszystkichKlientow().get(idKlienta);
    }

    ListaKlientow() {}

    public String dodajKlientaDoListy(Klient klient) {
        getListaWszystkichKlientow().add(klient);
        return "Pomyślnie dodano klienta " + klient.toString();
    }

    // Usuniecie klienta z listy
    public String usunKlienta(Klient klient) {
        getListaWszystkichKlientow().remove(klient);
        return "Pomyślnie usunięto klienta " + klient.toString();
    }

    public String usunKlienta(int id) {
        Klient tempKlient = getListaWszystkichKlientow().get(id);
        getListaWszystkichKlientow().remove(id);
        return "Pomyślnie usunięto klienta " + tempKlient.toString();
    }

    public String wykonajPrzelew(int idNadawcy, int idRachunkuNadawcy, int idOdiorcy, int idRachunkuOdbiorcy, double kwotaPrzelewu) {
        ArrayList<ZwroconeWartosci> listaWartosci= new ArrayList<ZwroconeWartosci>();

        listaWartosci = getListaWszystkichKlientow().get(idNadawcy).ustalPoprawnoscPrzelewu(kwotaPrzelewu, idRachunkuNadawcy, getListaWszystkichKlientow().get(idOdiorcy), idRachunkuOdbiorcy);

        if (listaWartosci.get(0).getKod() == 1 && listaWartosci.get(1).getKod() == 1) {
            Klient odbiorca = pobierzKlienta(idOdiorcy);
            odbiorca.getListaRachunkow().get(idRachunkuOdbiorcy).zwiekszSaldo(kwotaPrzelewu);
            odbiorca.getListaRachunkow().get(idRachunkuOdbiorcy).dodajWpisHistoriiTransakcji(listaWartosci.get(1).getWiadomosc() + "| Saldo : " + odbiorca.getListaRachunkow().get(idRachunkuOdbiorcy).getSaldo());

            Klient nadawca = pobierzKlienta(idNadawcy);
            nadawca.getListaRachunkow().get(idRachunkuNadawcy).zmniejszSaldo(kwotaPrzelewu + nadawca.getListaRachunkow().get(idRachunkuNadawcy).getKosztPrzelewu(kwotaPrzelewu));
            nadawca.getListaRachunkow().get(idRachunkuNadawcy).dodajWpisHistoriiTransakcji(listaWartosci.get(0).getWiadomosc() + "| Saldo : " + nadawca.getListaRachunkow().get(idRachunkuNadawcy).getSaldo());
            return "Pomyślnie wykonano przelew";
        }
        else {
            return "Nie można było wykonać przelewu";
        }
    }
    /**
     * Dodanie klienta o wskazanym imieniu i nazwisku
     */
    public void dodajKlienta(String Imie, String Nazwisko) {
        Klient tempKlient = new Klient(Imie, Nazwisko);
        dodajKlientaDoListy(tempKlient);
    }

    /**
     * Metoda ta pobiera od uzytkownika kwote pieniedzy
     */
    public double podajKwote() {
        Scanner tempScanner = new Scanner(System.in);
        double tempKwota;
        do {
            tempKwota = tempScanner.nextDouble();
            if (tempKwota < 0)
                System.out.println("Wprowadz kwote wieksza od 0");
        } while (tempKwota < 0);
        return tempKwota;
    }

    /**
     * Metoda pobiera z listy indeks klienta, na ktorym uzytkownik chce pracowac
     */
    public int podajIndeksKlienta() {
        Scanner tempScanner = new Scanner(System.in);
        boolean error = false;
        int tempNumerKlienta;
        do {
            tempNumerKlienta = tempScanner.nextInt();
            if (tempNumerKlienta < 0 || getListaWszystkichKlientow().size() < tempNumerKlienta) {
                error = true;
                System.out.println("\nKlient nie istnieje, podaj wartosc ponownie\n");
                drukujKlientow();
            } else
                error = false;
        } while (error == true);
        return tempNumerKlienta - 1;
    }

    /**
     * Metoda wypisuje liste klientow, wraz z ich indeksami
     */
    public void drukujKlientow() {
        System.out.println("Lista wszystkich klientow : ");
        for (int i = 0; i < getListaWszystkichKlientow().size(); i++)
            System.out.println(i + 1 + ". " + getListaWszystkichKlientow().get(i).getImie() + " " + getListaWszystkichKlientow().get(i).getNazwisko());
    }

    /**
     * Metoda zapisuje do pliku klientow ich rachunki oraz historie operacji
     */
    public void zapiszDaneDoPliku() throws IOException {
        FileOutputStream f = new FileOutputStream("myObjects.txt");
        ObjectOutputStream o = new ObjectOutputStream(f);

        o.writeObject(this.getListaWszystkichKlientow());
        o.close();
        f.close();
    }

    /**
     * metoda wczytuje klientow, rachunki oraz historie operacji
     */
    public ArrayList<Klient> wczytajDaneZPliku() throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream("myObjects.txt");
        ObjectInputStream oi = new ObjectInputStream(fi);

        ArrayList<Klient> lista = (ArrayList<Klient>) (oi.readObject());
        oi.close();

        return lista;

    }
}

class ButtonWindow {
    Window firstWindow;
    Button firstButton;
    TextBox textBox;
    int someInt;
    Window lastWindow;
    Button lastButton;
    ButtonWindow() {}


    public Window getFirstWindow() {
        return firstWindow;
    }

    public Button getLastButton() {
        return lastButton;
    }

    public int getSomeInt() {
        return someInt;
    }

    public TextBox getTextBox() {
        return textBox;
    }

    public Window getLastWindow() {
        return lastWindow;
    }

    public Button getFirstButton() {
        return firstButton;
    }



    public void setFirstWindow(Window firstWindow) {
        this.firstWindow = firstWindow;
    }

    public void setLastButton(Button lastButton) {
        this.lastButton = lastButton;
    }

    public void setSomeInt(int someInt) {
        this.someInt = someInt;
    }

    public void setTextBox(TextBox textBox) {
        this.textBox = textBox;
    }

    public void setLastWindow(Window lastWindow) {
        this.lastWindow = lastWindow;
    }

    public void setFirstButton(Button firstButton) {
        this.firstButton = firstButton;
    }

    /**
     * ======================================================================================
     *
     * FrontEnd
     *
     * ======================================================================================
     */
}
public class BankAPP implements Serializable{
    public static void FrontEndTextMenu(ListaKlientow listaKlientow) {
        /**
         * Okno główne aplikacji
         */
        Terminal mainScreen = TerminalFacade.createTerminal(Charset.forName("UTF8"));
        final GUIScreen guiScreen = TerminalFacade.createGUIScreen();
        Window appWindow = new Window("Aplikacja bankowa");
        appWindow.setWindowSizeOverride(new TerminalSize(1300, 500));

        Table euroTable = tableASCIIComponent(1);
        appWindow.addComponent(euroTable);


        appWindow.setWindowSizeOverride(new TerminalSize(1300,500));

        /**
         * Okno główne - menu wyboru operacji
         */



        Panel panelMainMenu = new Panel("Wybierz operację");
        panelMainMenu.setLayoutManager(new VerticalLayout());


        appWindow.addComponent(panelMainMenu);


        /**
         * Pozycja #1 - okno wyświetlenie listy klientów
         */

        ButtonWindow pos1BW = pos1WindowAndItsContent(guiScreen, listaKlientow);
        Button Pos1 = pos1BW.getLastButton();
        panelMainMenu.addComponent(Pos1);

        /**
         * Pozycja #2 - dodawanie klienta
         */

        ButtonWindow pos2BW = pos2WindowAndItsContent(guiScreen, listaKlientow);
        Button Pos2 = pos2BW.getLastButton();
        panelMainMenu.addComponent(Pos2);

        /**
         * Pozycja #3 - usuwanie klienta
         */

        ButtonWindow pos3BW = pos3WindowAndItsContent(guiScreen, listaKlientow);
        Button Pos3 = pos3BW.getLastButton();
        panelMainMenu.addComponent(Pos3);

        /**
         * Pozycja #4 - wypisanie rachunków klienta
         */

        ButtonWindow pos4BW = pos4WindowAndItsContent(guiScreen, listaKlientow);
        Button Pos4 = pos4BW.getFirstButton();
        panelMainMenu.addComponent(Pos4);

        /**
         * Pozycja #5 - Dodaj rachunek
         */
        ButtonWindow pos5BW = pos5WindowAndItsContent(guiScreen, listaKlientow);
        Button pos5FirstButton = pos5BW.getFirstButton();
        panelMainMenu.addComponent(pos5FirstButton);

        /**
         * Pozycja #6 - Usuń rachunek
         */

        ButtonWindow pos6BW = pos6WindowAndItsContent(guiScreen, listaKlientow);
        Button Pos6 = pos6BW.getFirstButton();
        panelMainMenu.addComponent(Pos6);

        /**
         * Pozycja #7 - Wyświetl historię transakcji
         */

        ButtonWindow pos7BW = pos7WindowAndItsContent(guiScreen, listaKlientow);
        Button Pos7 = pos7BW.getFirstButton();
        panelMainMenu.addComponent(Pos7);

        /**
         * Pozycja #8 - Wpłata gotówki
         */

        ButtonWindow pos8BW = pos8WindowAndItsContent(guiScreen, listaKlientow);
        Button Pos8 = pos8BW.getFirstButton();
        panelMainMenu.addComponent(Pos8);

        /**
         * Pozycja #9 - Wypłata gotówki
         */

        ButtonWindow pos9BW = pos9WindowAndItsContent(guiScreen, listaKlientow);
        Button Pos9 = pos9BW.getFirstButton();
        panelMainMenu.addComponent(Pos9);

        /**
         * Pozycja #10 - Przelew
         */

        ButtonWindow pos10BW = pos10WindowAndItsContent(guiScreen, listaKlientow);
        Button Pos10 = pos10BW.getFirstButton();
        panelMainMenu.addComponent(Pos10);

        /**
         * Pozycja #11 - Zapis/odczyt z pliku
         */

        ButtonWindow pos11BW = pos11WindowAndItsContent(guiScreen, listaKlientow);
        Button Pos11 = pos11BW.getFirstButton();
        panelMainMenu.addComponent(Pos11);

        /**
         * Generowanie całego gui
         */

        guiScreen.getScreen().startScreen();
        guiScreen.showWindow(appWindow);
        guiScreen.getScreen().stopScreen();
    }


    /**
     * Metoda tworząca przycisk wychodzący z danego menu
     */
    public static Button exitButtonRegular(String nazwa, Window oknoDoZamknieca) {
        Button exitBtn = new Button(nazwa, new Action() {

            public void doAction() {
                oknoDoZamknieca.close();
            }
        });
        return exitBtn;
    }

    /**
     * Metoda tworząca tabelę przechowującą listę klientów
     */
    public static Table tableClientsList(ListaKlientow listaKlientow) {
        int rozmiarListyKlientow = listaKlientow.getListaWszystkichKlientow().size();
        Table tabelaPomocniczaListyKlientow = new Table(rozmiarListyKlientow);
        if (rozmiarListyKlientow == 0) {
            Component[] pustePole = new Component[1];
            pustePole[0] = new Label("");
            tabelaPomocniczaListyKlientow.addRow(pustePole);
        }
        else {
            for (int i = 0 ;i < rozmiarListyKlientow ; i++) {
                Component[] rzedyListyPracownikow = new Component[1];
                rzedyListyPracownikow[0] = new Label((i+1) + ". "+ listaKlientow.getListaWszystkichKlientow().get(i).getImie() +" "+listaKlientow.getListaWszystkichKlientow().get(i).getNazwisko()) ;
                tabelaPomocniczaListyKlientow.addRow(rzedyListyPracownikow);
            }
        }
        return tabelaPomocniczaListyKlientow;
    }

    /**
     * Metoda tworząca nowe okno z określonymi ustawieniami
     */
    public static Window windowCreteSubWindow(String nazwa) {
        final Window oknoListyKlientow = new Window(nazwa);
        oknoListyKlientow.setWindowSizeOverride(new TerminalSize(1300, 500));
        return oknoListyKlientow;
    }


    public static ButtonWindow pos1WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {

        ButtonWindow returnBW = new ButtonWindow();

        /**
         *
         */
        Button buttonPos1ListaKlientow = new Button("1. Lista klientów",new Action() {

            @Override

            /**
             * Wciśnięcie przycisku pobierze dane z listy klientów i je wyświetli
             */
            public void doAction() {

                /**
                 * Stworzenie okna głównego kryjącego się pod pozycją pierwszą
                 */
                Window pos1Window = windowCreteSubWindow("Lista klientów");

                /**
                 * Dodanie tego okna do BW
                 */
                returnBW.setFirstWindow(pos1Window);

                /**
                 * usunięcie poprzedniej listy
                 */
                pos1Window.removeAllComponents();

                /**
                 * wygenerowanie i dodanie nowej listy klientów
                 */
                Table tableClients = tableClientsList(listaKlientow);
                pos1Window.addComponent(tableClients);

                /**
                 * Button Zamknij okno klientów
                 */
                Button pos1ExitButton = exitButtonRegular("Zamknij okno listy klientów" ,pos1Window);
                pos1ExitButton.setAlignment(Component.Alignment.RIGHT_CENTER);
                pos1Window.addComponent(pos1ExitButton);


                guiScreen.showWindow(pos1Window);
            }
        });

        buttonPos1ListaKlientow.setAlignment(Component.Alignment.LEFT_CENTER);
        returnBW.setLastButton(buttonPos1ListaKlientow);
        return returnBW;
    }

    public static ButtonWindow pos2WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {

        ButtonWindow Pos2BW = new ButtonWindow();

        Button buttonPos2AddClient = new Button("2. Dodaj Klienta", new Action() {

            @Override
            public void doAction() {
                Window windowPos2AddDeleteClient = windowCreteSubWindow("Dodaj klienta - podaj dane");
                Pos2BW.setFirstWindow(windowPos2AddDeleteClient);

                /**
                 * Dodanie dwóch miejsc na wpisanie imienia i nazwiska
                 */
                TextBox imieTextBox = new TextBox();
                TextBox nazwiskoTextBox = new TextBox();
                windowPos2AddDeleteClient.addComponent(new Label("Imię"));
                windowPos2AddDeleteClient.addComponent(imieTextBox);
                windowPos2AddDeleteClient.addComponent(new Label("Nazwisko"));
                windowPos2AddDeleteClient.addComponent(nazwiskoTextBox);


                /**
                 * Przycisk, którego wciśnięcie doda nowego klienta
                 */
                Button buttonPos2AddClientExit = new Button("Dodaj klienta i wyjdź" ,new Action() {
                    @Override
                    public void doAction() {
                        String imieString = imieTextBox.getText();
                        String nazwiskoString = nazwiskoTextBox.getText();
                        if (!(imieTextBox.getText() == "" || nazwiskoTextBox.getText() == "")) {
                            Klient tempKlient =  new Klient(imieString, nazwiskoString);
                            listaKlientow.dodajKlientaDoListy(tempKlient);
                        }

                        imieTextBox.setText("");
                        nazwiskoTextBox.setText("");

                        windowPos2AddDeleteClient.close();

                    }
                });
                buttonPos2AddClientExit.setAlignment(Component.Alignment.LEFT_CENTER);

                windowPos2AddDeleteClient.addComponent(buttonPos2AddClientExit);

                guiScreen.showWindow(windowPos2AddDeleteClient);
            }
        });
        Pos2BW.setLastButton(buttonPos2AddClient);

        return Pos2BW;
    }

    public static ButtonWindow pos3WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {

        ButtonWindow Pos3BW = new ButtonWindow();

        /**
         * Dodaj przycisk 3. Usuń klienta
         */
        Button buttonPos3DeleteClient = new Button("3. Usuń klienta" ,new Action() {
            @Override
            public void doAction() {
                Window windowPos3 = windowCreteSubWindow("Usuń klienta - podaj jego numer z listy");
                windowPos3.removeAllComponents();

                /**
                 * Dodanie listy klientów
                 */
                Table tableListOfClientsPos3 = tableClientsList(listaKlientow);
                windowPos3.addComponent(tableListOfClientsPos3);

                TextBox clientIdDelete = new TextBox();
                windowPos3.addComponent(clientIdDelete);

                /**
                 * Przycisk - Usuń i zamknij okno
                 */
                Button buttonPos3Exit = new Button("Usuń i klienta i zamknij te okno", new Action() {

                    public void doAction() {
                        String TextBoxInput = clientIdDelete.getText();

                        if (isNumeric(TextBoxInput)) {
                            if (Integer.parseInt(TextBoxInput) <= listaKlientow.getListaWszystkichKlientow().size()) {
                                listaKlientow.usunKlienta(Integer.parseInt(TextBoxInput)-1);
                            }
                        }

                        windowPos3.close();
                    }
                });

                buttonPos3Exit.setAlignment(Component.Alignment.RIGHT_CENTER);
                windowPos3.addComponent(buttonPos3Exit);

                guiScreen.showWindow(windowPos3);
            }
        });

        buttonPos3DeleteClient.setAlignment(Component.Alignment.LEFT_CENTER);

        Pos3BW.setLastButton(buttonPos3DeleteClient);

        return Pos3BW;
    }
    
    public static ButtonWindow pos4WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {

        Window windowPos4 = windowCreteSubWindow("Lista rachunków - wpisz numer klienta");
        ButtonWindow pos4ReturnBW = new ButtonWindow();
        /**
         * Przycisk przejścia do listy klientów
          */

        Button buttonPos4DisplayAccounts = new Button("4. Lista rachunków klienta",new Action() {
            @Override
            public void doAction() {

                windowPos4.removeAllComponents();

                /**
                 * // Lista klientów tak jak z pozycji 1
                 */

                Table tablePos4ClientList = tableClientsList(listaKlientow);
                windowPos4.addComponent(tablePos4ClientList);

                /**
                 * Miejsce na wpisanie numeru klienta
                 */
                TextBox tbClientID = new TextBox();
                windowPos4.addComponent(tbClientID);

                /**
                 * Przycisk Wyświetlający listę rachunków, zamykający listę pracowników
                 */
                Button pos4ListaButton = new Button("Wyświetl rachunki klienta" , new Action() {

                    public void doAction() {

                        if (isNumeric(tbClientID.getText())) {
                            int clientIdToDelete = Integer.parseInt(tbClientID.getText()) - 1;
                            if (clientIdToDelete <= listaKlientow.getListaWszystkichKlientow().size()) {
                                String composeNameString = "Lista rachunkow klienta " + listaKlientow.getListaWszystkichKlientow().get(clientIdToDelete).getImie() + " " + listaKlientow.getListaWszystkichKlientow().get(clientIdToDelete).getNazwisko();

                                /**
                                 * Stwórz okno listy rachunków
                                 */
                                Window pos4ListaRachunkow = windowCreteSubWindow(composeNameString);
                                pos4ListaRachunkow.removeAllComponents();

                                /**
                                 * Tabela która zostanie dodana do tego okna
                                 */
                                ArrayList<Rachunek> listaRachunkowKlienta = listaKlientow.getListaWszystkichKlientow().get(clientIdToDelete).getListaRachunkow();
                                int rozmiarListyRachunkow = listaRachunkowKlienta.size();
                                Table tableContent = new Table(rozmiarListyRachunkow);
                                if (rozmiarListyRachunkow == 0) {
                                    Component[] pustePole = new Component[1];
                                    pustePole[0] = new Label("Ten klient nie posiada żadnego rachunku");
                                    tableContent.addRow(pustePole);
                                }
                                else {
                                    for (int i = 0 ;i < rozmiarListyRachunkow ; i++) {
                                        Component[] rzedyListyRachunkow = new Component[1];
                                        rzedyListyRachunkow[0] = new Label((i+1) + ". "+ listaRachunkowKlienta.get(i).getNazwaRachunku() + " Saldo : " + listaRachunkowKlienta.get(i).getSaldo()) ;
                                        tableContent.addRow(rzedyListyRachunkow);
                                    }
                                }

                                /**
                                 * Dodanie tej tabeli do okna
                                 */
                                pos4ListaRachunkow.addComponent(tableContent);

                                /**
                                 * Przycisk zamykający listę rachunków
                                 */
                                Button pos4ListaRachunkowExit = new Button("Zamknij okno rachunków",new Action() {

                                    public void doAction() {
                                        windowPos4.close();
                                        pos4ListaRachunkow.close();

                                    }
                                });
                                pos4ReturnBW.setLastButton(pos4ListaRachunkowExit);
                                pos4ReturnBW.setLastWindow(pos4ListaRachunkow);
                                pos4ListaRachunkowExit.setAlignment(Component.Alignment.RIGHT_CENTER);
                                pos4ListaRachunkow.addComponent(pos4ListaRachunkowExit);
                                guiScreen.showWindow(pos4ListaRachunkow);
                            }
                        }
                        
                    }
                });
                
                pos4ListaButton.setAlignment(Component.Alignment.RIGHT_CENTER);
                windowPos4.addComponent(pos4ListaButton);

                guiScreen.showWindow(windowPos4);
            }
        });

        pos4ReturnBW.setFirstWindow(windowPos4);
        pos4ReturnBW.setFirstButton(buttonPos4DisplayAccounts);

        
        buttonPos4DisplayAccounts.setAlignment(Component.Alignment.LEFT_CENTER);

        return pos4ReturnBW;
    }

    public static ButtonWindow pos5WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {

        ButtonWindow returnBW = new ButtonWindow();

        /**
         *
         */
        Button buttonPos5ListaKlientow = new Button("5. Dodaj rachunek",new Action() {

            @Override

            /**
             * Wciśnięcie przycisku pobierze dane z listy klientów i je wyświetli
             */
            public void doAction() {

                /**
                 * Stworzenie okna głównego kryjącego się pod pozycją pierwszą
                 */
                Window pos5Window = windowCreteSubWindow("Dodaj rachunek - lista klientów");

                /**
                 * Dodanie tego okna do BW
                 */
                returnBW.setFirstWindow(pos5Window);

                /**
                 * usunięcie poprzedniej listy
                 */
                pos5Window.removeAllComponents();

                /**
                 * wygenerowanie i dodanie nowej listy klientów
                 */
                Table tableClients = tableClientsList(listaKlientow);
                pos5Window.addComponent(tableClients);


                Component[] podajNumerKlienta = new Component[1];
                podajNumerKlienta[0] = new Label("Podaj numer klienta");
                pos5Window.addComponent(podajNumerKlienta[0]);

                TextBox tbPodajNumerKlienta = new TextBox();
                pos5Window.addComponent(tbPodajNumerKlienta);

                Component[] emptySpace = new Component[1];
                emptySpace[0] = new Label("----------------------------------");
                pos5Window.addComponent(emptySpace[0]);

                Component[] tekstRachunkuZwykłego = new Component[1];
                tekstRachunkuZwykłego[0] = new Label("1 - Rachunek zwykły");
                pos5Window.addComponent(tekstRachunkuZwykłego[0]);

                Component[] tekstRachunkuOszczedniosciowego = new Component[1];
                tekstRachunkuOszczedniosciowego[0] = new Label("2 - Rachunek oszczędnościowy");
                pos5Window.addComponent(tekstRachunkuOszczedniosciowego[0]);

                Component[] podajNumerRachunku = new Component[1];
                podajNumerRachunku[0] = new Label("Podaj numer rachunku");
                pos5Window.addComponent(podajNumerRachunku[0]);

                TextBox tbKtoryRachunek = new TextBox();
                pos5Window.addComponent(tbKtoryRachunek);
                pos5Window.addComponent(emptySpace[0]);

                Component[] nazwaRachunku = new Component[1];
                nazwaRachunku[0] = new Label("Podaj nazwę rachunku");
                pos5Window.addComponent(nazwaRachunku[0]);

                TextBox tbNazwaRachunku = new TextBox();
                pos5Window.addComponent(tbNazwaRachunku);


                /**
                 * Button Zamknij okno klientów i dodaj rachunek
                 */
                Button pos5ExitButton = new Button("Zamknij okno listy klientów", new Action() {
                    @Override
                    public void doAction() {

                        if (isNumeric(tbKtoryRachunek.getText()) && isNumeric(tbPodajNumerKlienta.getText())) {
                            int ktoryRachunekId = Integer.parseInt(tbKtoryRachunek.getText());
                            int ktoryKlientId = Integer.parseInt(tbPodajNumerKlienta.getText()) - 1;

                            if (ktoryKlientId < listaKlientow.getListaWszystkichKlientow().size()) {
                                if (ktoryRachunekId == 1) {
                                    listaKlientow.getListaWszystkichKlientow().get(ktoryKlientId).dodajRachunekZwykly(tbNazwaRachunku.getText());
                                }
                                else if (ktoryRachunekId == 2) {
                                    listaKlientow.getListaWszystkichKlientow().get(ktoryKlientId).dodajRachunekOszczednosciowy(tbNazwaRachunku.getText());
                                }
                            }
                        }
                        pos5Window.close();
                    }
                });

                pos5ExitButton.setAlignment(Component.Alignment.RIGHT_CENTER);
                pos5Window.addComponent(pos5ExitButton);


                guiScreen.showWindow(pos5Window);
            }
        });

        buttonPos5ListaKlientow.setAlignment(Component.Alignment.LEFT_CENTER);
        returnBW.setFirstButton(buttonPos5ListaKlientow);
        return returnBW;
    }

    public static ButtonWindow pos6WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {

        Window windowPos6 = windowCreteSubWindow("Usuń klienta - podaj numer z listy");
        ButtonWindow pos6ReturnBW = new ButtonWindow();
        /**
         * Przycisk przejścia do listy klientów
         */

        Button buttonPos6DisplayAccounts = new Button("6. Usuń rachunek",new Action() {
            @Override
            public void doAction() {

                windowPos6.removeAllComponents();

                /**
                 * // Lista klientów tak jak z pozycji 1
                 */

                Table tablePos6ClientList = tableClientsList(listaKlientow);
                windowPos6.addComponent(tablePos6ClientList);

                /**
                 * Miejsce na wpisanie numeru klienta
                 */
                TextBox tbClientID = new TextBox();
                windowPos6.addComponent(tbClientID);

                /**
                 * Przycisk Wyświetlający listę rachunków, zamykający listę pracowników
                 */
                Button pos6ListaButton = new Button("Wyświetl rachunki klienta - wybierz jeden do usunięcia" , new Action() {

                    public void doAction() {

                        if (isNumeric(tbClientID.getText())) {
                            int clientIdToDelete = Integer.parseInt(tbClientID.getText()) - 1;
                            if (clientIdToDelete <= listaKlientow.getListaWszystkichKlientow().size()) {
                                String composeNameString = "Lista rachunkow klienta " + listaKlientow.getListaWszystkichKlientow().get(clientIdToDelete).getImie() + " " + listaKlientow.getListaWszystkichKlientow().get(clientIdToDelete).getNazwisko() + " - podaj numer konta które chcesz usunąć";

                                /**
                                 * Stwórz okno listy rachunków
                                 */
                                Window pos6ListaRachunkow = windowCreteSubWindow(composeNameString);
                                pos6ListaRachunkow.removeAllComponents();

                                /**
                                 * Tabela, która zostanie dodana do tego okna
                                 */
                                ArrayList<Rachunek> listaRachunkowKlienta = listaKlientow.getListaWszystkichKlientow().get(clientIdToDelete).getListaRachunkow();
                                int rozmiarListyRachunkow = listaRachunkowKlienta.size();
                                Table tableContent = new Table(rozmiarListyRachunkow);
                                if (rozmiarListyRachunkow == 0) {
                                    Component[] pustePole = new Component[1];
                                    pustePole[0] = new Label("Ten klient nie posiada żadnego rachunku");
                                    tableContent.addRow(pustePole);
                                }
                                else {
                                    for (int i = 0 ;i < rozmiarListyRachunkow ; i++) {
                                        Component[] rzedyListyRachunkow = new Component[1];
                                        rzedyListyRachunkow[0] = new Label((i+1) + ". "+ listaRachunkowKlienta.get(i).getNazwaRachunku()  + " Saldo : " + listaRachunkowKlienta.get(i).getSaldo()) ;
                                        tableContent.addRow(rzedyListyRachunkow);
                                    }
                                }
                                /**
                                 * Dodanie tej tabeli do okna
                                 */
                                pos6ListaRachunkow.addComponent(tableContent);

                                TextBox tbRemoveAccount = new TextBox();
                                pos6ListaRachunkow.addComponent(tbRemoveAccount);

                                /**
                                 * Przycisk zamykający listę rachunków
                                 */
                                Button pos6ListaRachunkowExit = new Button("Usuń rachunek i wróć do menu głównego",new Action() {

                                    public void doAction() {

                                        String accountIdFromTextBox = tbRemoveAccount.getText();

                                        if (isNumeric(accountIdFromTextBox)) {
                                            int accountIdToDelete = Integer.parseInt(accountIdFromTextBox) - 1;
                                            if (accountIdToDelete < listaKlientow.getListaWszystkichKlientow().get(clientIdToDelete).getListaRachunkow().size()) {
                                                listaKlientow.getListaWszystkichKlientow().get(clientIdToDelete).usunRachunek(accountIdToDelete);
                                            }
                                        }

                                        windowPos6.close();
                                        pos6ListaRachunkow.close();

                                    }
                                });
                                pos6ReturnBW.setLastButton(pos6ListaRachunkowExit);
                                pos6ReturnBW.setLastWindow(pos6ListaRachunkow);
                                pos6ListaRachunkowExit.setAlignment(Component.Alignment.RIGHT_CENTER);
                                pos6ListaRachunkow.addComponent(pos6ListaRachunkowExit);
                                guiScreen.showWindow(pos6ListaRachunkow);
                            }
                        }

                    }
                });

                pos6ListaButton.setAlignment(Component.Alignment.RIGHT_CENTER);
                windowPos6.addComponent(pos6ListaButton);

                guiScreen.showWindow(windowPos6);
            }
        });

        pos6ReturnBW.setFirstWindow(windowPos6);
        pos6ReturnBW.setFirstButton(buttonPos6DisplayAccounts);


        buttonPos6DisplayAccounts.setAlignment(Component.Alignment.LEFT_CENTER);

        return pos6ReturnBW;
    }

    public static ButtonWindow pos7WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {

        Window windowPos7 = windowCreteSubWindow("Historia transakcji - wpisz numer klienta");
        ButtonWindow pos7ReturnBW = new ButtonWindow();
        /**
         * Przycisk przejścia do listy klientów
         */

        Button buttonPos7DisplayAccounts = new Button("7. Historia transakcji klienta",new Action() {
            @Override
            public void doAction() {

                windowPos7.removeAllComponents();

                /**
                 * // Lista klientów tak jak z pozycji 1
                 */

                Table tablePos7ClientList = tableClientsList(listaKlientow);
                windowPos7.addComponent(tablePos7ClientList);

                /**
                 * Miejsce na wpisanie numeru klienta
                 */
                TextBox tbClientId = new TextBox();
                windowPos7.addComponent(tbClientId);

                /**
                 * Przycisk Wyświetlający listę rachunków, zamykający listę pracowników
                 */
                Button pos7ListaButton = new Button("Wyświetl rachunki klienta" , new Action() {

                    public void doAction() {

                        String TextBoxInput = tbClientId.getText();

                        if (isNumeric(TextBoxInput)) {
                            int clientId = Integer.parseInt(TextBoxInput) - 1;
                            if (clientId <= listaKlientow.getListaWszystkichKlientow().size()) {
                                String composeNameString = "Lista rachunkow klienta " + listaKlientow.getListaWszystkichKlientow().get(clientId).getImie() + " " + listaKlientow.getListaWszystkichKlientow().get(clientId).getNazwisko();

                                /**
                                 * Stwórz okno listy rachunków
                                 */
                                Window pos7ListaRachunkow = windowCreteSubWindow(composeNameString);
                                pos7ListaRachunkow.removeAllComponents();

                                /**
                                 * Tabela która zostanie dodana do tego okna
                                 */
                                ArrayList<Rachunek> listaRachunkowKlienta = listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow();
                                int rozmiarListyRachunkow = listaRachunkowKlienta.size();
                                Table tableContent = new Table(rozmiarListyRachunkow);
                                if (rozmiarListyRachunkow == 0) {
                                    Component[] pustePole = new Component[1];
                                    pustePole[0] = new Label("Ten klient nie posiada żadnego rachunku");
                                    tableContent.addRow(pustePole);
                                }
                                else {
                                    for (int i = 0 ;i < rozmiarListyRachunkow ; i++) {
                                        Component[] rzedyListyRachunkow = new Component[1];
                                        rzedyListyRachunkow[0] = new Label((i+1) + ". "+ listaRachunkowKlienta.get(i).getNazwaRachunku()  + " Saldo : " + listaRachunkowKlienta.get(i).getSaldo()) ;
                                        tableContent.addRow(rzedyListyRachunkow);
                                    }
                                }

                                /**
                                 * Dodanie tej tabeli do okna
                                 */
                                pos7ListaRachunkow.addComponent(tableContent);

                                pos7ListaRachunkow.addComponent(new EmptySpace());


                                Component[] podajNumerRachunku = new Component[1];
                                podajNumerRachunku[0] = new Label("Podaj numer rachunku");
                                pos7ListaRachunkow.addComponent(podajNumerRachunku[0]);

                                TextBox tbPodajNumerRachunku = new TextBox();
                                pos7ListaRachunkow.addComponent(tbPodajNumerRachunku);


                                /**
                                 * Button Zamknij okno klientów
                                 */
                                Button pos7ListaRachunkowExit = new Button("Pokaż historię operacji", new Action() {
                                    @Override
                                    public void doAction() {
                                        if (isNumeric(tbPodajNumerRachunku.getText())) {
                                            int idRachunku = Integer.parseInt(tbPodajNumerRachunku.getText()) - 1;
                                            int numberOfAccounts = listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow().size();
                                            int historyListSize = listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow().get(idRachunku).getHistoriaTransakcji().size();
                                            if (idRachunku < numberOfAccounts) {
                                                Window windowHistoriaOperacji = windowCreteSubWindow("Historia transakcji");

                                                Table tabelaHistorii = new Table();
                                                if (historyListSize == 0) {
                                                    Component[] brakRachunkow = new Component[1];
                                                    brakRachunkow[0] = new Label("Klient nie posiada historii transakcji na tym rachunku");
                                                    windowHistoriaOperacji.addComponent(brakRachunkow[0]);
                                                }
                                                for (int i = 0; i < historyListSize ; i++) {
                                                    Component[] tableComponent = new Component[1];
                                                    tableComponent[0] = new Label(i + 1 + ". " + listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow().get(idRachunku).getHistoriaTransakcji().get(i));
                                                    tabelaHistorii.addRow(tableComponent[0]);
                                                }
                                                windowHistoriaOperacji.addComponent(tabelaHistorii);


                                                Button exit7LastWindow = new Button("Wyjdź do menu", new Action() {
                                                    @Override
                                                    public void doAction() {
                                                        windowPos7.close();
                                                        pos7ListaRachunkow.close();
                                                        windowHistoriaOperacji.close();
                                                    }
                                                });
                                                windowHistoriaOperacji.addComponent(exit7LastWindow);
                                                guiScreen.showWindow(windowHistoriaOperacji);
                                            }

                                        }

                                    }
                                });

                                pos7ReturnBW.setLastButton(pos7ListaRachunkowExit);
                                pos7ReturnBW.setLastWindow(pos7ListaRachunkow);

                                pos7ListaRachunkowExit.setAlignment(Component.Alignment.RIGHT_CENTER);
                                pos7ListaRachunkow.addComponent(pos7ListaRachunkowExit);
                                guiScreen.showWindow(pos7ListaRachunkow);
                            }
                        }

                    }
                });

                pos7ListaButton.setAlignment(Component.Alignment.RIGHT_CENTER);
                windowPos7.addComponent(pos7ListaButton);

                guiScreen.showWindow(windowPos7);
            }
        });

        pos7ReturnBW.setFirstWindow(windowPos7);
        pos7ReturnBW.setFirstButton(buttonPos7DisplayAccounts);


        buttonPos7DisplayAccounts.setAlignment(Component.Alignment.LEFT_CENTER);

        return pos7ReturnBW;
    }

    public static ButtonWindow pos8WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {

        Window windowPos8 = windowCreteSubWindow("Wpłać gotówkę - wpisz numer klienta");
        ButtonWindow pos8ReturnBW = new ButtonWindow();
        /**
         * Przycisk przejścia do listy klientów
         */

        Button buttonPos8DisplayAccounts = new Button("8. Wpłać gotówkę na rachunek",new Action() {
            @Override
            public void doAction() {

                windowPos8.removeAllComponents();

                /**
                 * // Lista klientów tak jak z pozycji 1
                 */

                Table tablePos8ClientList = tableClientsList(listaKlientow);
                windowPos8.addComponent(tablePos8ClientList);

                /**
                 * Miejsce na wpisanie numeru klienta
                 */
                TextBox tbClientId = new TextBox();
                windowPos8.addComponent(tbClientId);

                /**
                 * Przycisk Wyświetlający listę rachunków, zamykający listę pracowników
                 */
                Button pos8ListaButton = new Button("Wyświetl rachunki klienta" , new Action() {

                    public void doAction() {

                        if (isNumeric(tbClientId.getText())) {
                            int clientId = Integer.parseInt(tbClientId.getText()) - 1;
                            if (clientId <= listaKlientow.getListaWszystkichKlientow().size()) {
                                String composeNameString = "Lista rachunkow klienta " + listaKlientow.getListaWszystkichKlientow().get(clientId).getImie() + " " + listaKlientow.getListaWszystkichKlientow().get(clientId).getNazwisko();

                                /**
                                 * Stwórz okno listy rachunków
                                 */
                                Window pos8ListaRachunkow = windowCreteSubWindow(composeNameString);
                                pos8ListaRachunkow.removeAllComponents();

                                /**
                                 * Tabela która zostanie dodana do tego okna
                                 */
                                ArrayList<Rachunek> listaRachunkowKlienta = listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow();
                                int rozmiarListyRachunkow = listaRachunkowKlienta.size();
                                Table tableContent = new Table(rozmiarListyRachunkow);
                                if (rozmiarListyRachunkow == 0) {
                                    Component[] pustePole = new Component[1];
                                    pustePole[0] = new Label("Ten klient nie posiada żadnego rachunku");
                                    tableContent.addRow(pustePole);
                                }
                                else {
                                    for (int i = 0 ;i < rozmiarListyRachunkow ; i++) {
                                        Component[] rzedyListyRachunkow = new Component[1];
                                        rzedyListyRachunkow[0] = new Label((i+1) + ". "+ listaRachunkowKlienta.get(i).getNazwaRachunku() + " Saldo : " + listaRachunkowKlienta.get(i).getSaldo()) ;
                                        tableContent.addRow(rzedyListyRachunkow);
                                    }
                                }

                                /**
                                 * Dodanie tej tabeli do okna
                                 */
                                pos8ListaRachunkow.addComponent(tableContent);

                                pos8ListaRachunkow.addComponent(new EmptySpace());


                                Component[] podajNumerRachunku = new Component[1];
                                podajNumerRachunku[0] = new Label("Podaj numer rachunku");
                                pos8ListaRachunkow.addComponent(podajNumerRachunku[0]);

                                TextBox tbPodajNumerRachunku = new TextBox();
                                pos8ListaRachunkow.addComponent(tbPodajNumerRachunku);

                                pos8ListaRachunkow.addComponent(new EmptySpace());
                                pos8ListaRachunkow.addComponent(new Label("Podaj kwotę"));
                                TextBox tbIleGotowkiWplacic = new TextBox();
                                pos8ListaRachunkow.addComponent(tbIleGotowkiWplacic);


                                /**
                                 * Button Zamknij okno klientów
                                 */
                                Button pos8ListaRachunkowExit = new Button("Wpłać gotówkę", new Action() {
                                    @Override
                                    public void doAction() {
                                        if (isNumeric(tbPodajNumerRachunku.getText()) && isNumeric(tbIleGotowkiWplacic.getText())) {
                                            int idRachunku = Integer.parseInt(tbPodajNumerRachunku.getText()) - 1;
                                            int numberOfAccounts = listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow().size();

                                            String coZwrocilo = "";
                                            if (idRachunku <= numberOfAccounts) {
                                               coZwrocilo =  listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow().get(idRachunku).zwiekszSaldo(Integer.parseInt(tbIleGotowkiWplacic.getText())).getWiadomosc();

                                            }
                                            else {

                                            }
                                            listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow().get(idRachunku).dodajWpisHistoriiTransakcji(coZwrocilo);
                                            windowPos8.close();
                                            pos8ListaRachunkow.close();
                                        }

                                    }
                                });

                                pos8ReturnBW.setLastButton(pos8ListaRachunkowExit);
                                pos8ReturnBW.setLastWindow(pos8ListaRachunkow);

                                pos8ListaRachunkowExit.setAlignment(Component.Alignment.RIGHT_CENTER);
                                pos8ListaRachunkow.addComponent(pos8ListaRachunkowExit);
                                guiScreen.showWindow(pos8ListaRachunkow);
                            }
                        }

                    }
                });

                pos8ListaButton.setAlignment(Component.Alignment.RIGHT_CENTER);
                windowPos8.addComponent(pos8ListaButton);

                guiScreen.showWindow(windowPos8);
            }
        });

        pos8ReturnBW.setFirstWindow(windowPos8);
        pos8ReturnBW.setFirstButton(buttonPos8DisplayAccounts);


        buttonPos8DisplayAccounts.setAlignment(Component.Alignment.LEFT_CENTER);

        return pos8ReturnBW;
    }

    public static ButtonWindow pos9WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {

        Window windowPos9 = windowCreteSubWindow("Wypłać gotówkę - wpisz numer klienta");
        ButtonWindow pos9ReturnBW = new ButtonWindow();
        /**
         * Przycisk przejścia do listy klientów
         */

        Button buttonPos9DisplayAccounts = new Button("9. Wypłać gotówkę z rachunku",new Action() {
            @Override
            public void doAction() {

                windowPos9.removeAllComponents();

                /**
                 * // Lista klientów tak jak z pozycji 1
                 */

                Table tablePos9ClientList = tableClientsList(listaKlientow);
                windowPos9.addComponent(tablePos9ClientList);

                /**
                 * Miejsce na wpisanie numeru klienta
                 */
                TextBox tbClientId = new TextBox();
                windowPos9.addComponent(tbClientId);

                /**
                 * Przycisk Wyświetlający listę rachunków, zamykający listę pracowników
                 */
                Button pos9ListaButton = new Button("Wyświetl rachunki klienta" , new Action() {

                    public void doAction() {

                        if (isNumeric(tbClientId.getText())) {
                            int clientId = Integer.parseInt(tbClientId.getText()) - 1;
                            if (clientId <= listaKlientow.getListaWszystkichKlientow().size()) {
                                String composeNameString = "Lista rachunkow klienta " + listaKlientow.getListaWszystkichKlientow().get(clientId).getImie() + " " + listaKlientow.getListaWszystkichKlientow().get(clientId).getNazwisko();

                                /**
                                 * Stwórz okno listy rachunków
                                 */
                                Window pos9ListaRachunkow = windowCreteSubWindow(composeNameString);
                                pos9ListaRachunkow.removeAllComponents();

                                /**
                                 * Tabela która zostanie dodana do tego okna
                                 */
                                ArrayList<Rachunek> listaRachunkowKlienta = listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow();
                                int rozmiarListyRachunkow = listaRachunkowKlienta.size();
                                Table tableContent = new Table(rozmiarListyRachunkow);
                                if (rozmiarListyRachunkow == 0) {
                                    Component[] pustePole = new Component[1];
                                    pustePole[0] = new Label("Ten klient nie posiada żadnego rachunku");
                                    tableContent.addRow(pustePole);
                                }
                                else {
                                    for (int i = 0 ;i < rozmiarListyRachunkow ; i++) {
                                        Component[] rzedyListyRachunkow = new Component[1];
                                        rzedyListyRachunkow[0] = new Label((i+1) + ". "+ listaRachunkowKlienta.get(i).getNazwaRachunku() + " Saldo : " + listaRachunkowKlienta.get(i).getSaldo()) ;
                                        tableContent.addRow(rzedyListyRachunkow);
                                    }
                                }

                                /**
                                 * Dodanie tej tabeli do okna
                                 */
                                pos9ListaRachunkow.addComponent(tableContent);

                                pos9ListaRachunkow.addComponent(new EmptySpace());


                                Component[] podajNumerRachunku = new Component[1];
                                podajNumerRachunku[0] = new Label("Podaj numer rachunku");
                                pos9ListaRachunkow.addComponent(podajNumerRachunku[0]);

                                TextBox tbPodajNumerRachunku = new TextBox();
                                pos9ListaRachunkow.addComponent(tbPodajNumerRachunku);

                                pos9ListaRachunkow.addComponent(new EmptySpace());
                                pos9ListaRachunkow.addComponent(new Label("Podaj kwotę"));
                                TextBox tbIleGotowkiWyplacic = new TextBox();
                                pos9ListaRachunkow.addComponent(tbIleGotowkiWyplacic);


                                /**
                                 * Button Zamknij okno klientów
                                 */
                                Button pos9ListaRachunkowExit = new Button("Wypłać gotówkę", new Action() {
                                    @Override
                                    public void doAction() {
                                        if (isNumeric(tbPodajNumerRachunku.getText()) && isNumeric(tbIleGotowkiWyplacic.getText())) {
                                            int idRachunku = Integer.parseInt(tbPodajNumerRachunku.getText()) - 1;
                                            int numberOfAccounts = listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow().size();

                                            String coZwrocilo = "";
                                            if (idRachunku <= numberOfAccounts) {
                                                coZwrocilo =  listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow().get(idRachunku).zmniejszSaldo(Integer.parseInt(tbIleGotowkiWyplacic.getText())).getWiadomosc();

                                            }
                                            else {

                                            }
                                            listaKlientow.getListaWszystkichKlientow().get(clientId).getListaRachunkow().get(idRachunku).dodajWpisHistoriiTransakcji(coZwrocilo);
                                            windowPos9.close();
                                            pos9ListaRachunkow.close();
                                        }

                                    }
                                });

                                pos9ReturnBW.setLastButton(pos9ListaRachunkowExit);
                                pos9ReturnBW.setLastWindow(pos9ListaRachunkow);

                                pos9ListaRachunkowExit.setAlignment(Component.Alignment.RIGHT_CENTER);
                                pos9ListaRachunkow.addComponent(pos9ListaRachunkowExit);
                                guiScreen.showWindow(pos9ListaRachunkow);
                            }
                        }

                    }
                });

                pos9ListaButton.setAlignment(Component.Alignment.RIGHT_CENTER);
                windowPos9.addComponent(pos9ListaButton);

                guiScreen.showWindow(windowPos9);
            }
        });

        pos9ReturnBW.setFirstWindow(windowPos9);
        pos9ReturnBW.setFirstButton(buttonPos9DisplayAccounts);


        buttonPos9DisplayAccounts.setAlignment(Component.Alignment.LEFT_CENTER);

        return pos9ReturnBW;
    }

    public static ButtonWindow pos10WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {

        Window windowPos10 = windowCreteSubWindow("Wykonaj przelew");
        ButtonWindow pos10ReturnBW = new ButtonWindow();
        /**
         * Przycisk przejścia do listy klientów
         */

        Button buttonPos10DisplayAccounts = new Button("10. Wykonaj przelew",new Action() {
            @Override
            public void doAction() {

                windowPos10.removeAllComponents();

                /**
                 * // Lista klientów tak jak z pozycji 1
                 */

                Table tablePos10ClientList = tableClientsList(listaKlientow);
                windowPos10.addComponent(tablePos10ClientList);

                windowPos10.addComponent(new Label("Podaj numer nadawcy przelewu"));

                /**
                 * Miejsce na wpisanie numeru klienta
                 */
                TextBox tbNadawacaID = new TextBox();
                windowPos10.addComponent(tbNadawacaID);

                windowPos10.addComponent(new EmptySpace());

                windowPos10.addComponent(new Label("Podaj numer odbiorcy"));

                TextBox tbOdbiorcaID = new TextBox();
                windowPos10.addComponent(tbOdbiorcaID);

                /**
                 * Przycisk Wyświetlający listę rachunków, zamykający listę pracowników
                 */
                Button pos10ListaButton = new Button("Wyświetl rachunki klienta" , new Action() {

                    public void doAction() {
                        //windowPos10.close();


                        if (isNumeric(tbNadawacaID.getText()) && isNumeric(tbOdbiorcaID.getText())) {
                            int nadawcaID = Integer.parseInt(tbNadawacaID.getText()) - 1;
                            int odbiorcaID = Integer.parseInt(tbOdbiorcaID.getText()) - 1;
                            if (nadawcaID < listaKlientow.getListaWszystkichKlientow().size() && odbiorcaID < listaKlientow.getListaWszystkichKlientow().size()) {
                                String nadawcaString = "Lista rachunków nadawcy : " + listaKlientow.getListaWszystkichKlientow().get(nadawcaID).getImie() + " " + listaKlientow.getListaWszystkichKlientow().get(nadawcaID).getNazwisko();
                                String odbiorcaString = "Lista rachunków odbiorcy " + listaKlientow.getListaWszystkichKlientow().get(odbiorcaID).getImie() + " " + listaKlientow.getListaWszystkichKlientow().get(odbiorcaID).getNazwisko();
                                /**
                                 * Stwórz okno listy rachunków
                                 */
                                Window pos10ListaRachunkow = windowCreteSubWindow("Lista rachunków dwóch klientów");
                                pos10ListaRachunkow.removeAllComponents();

                                pos10ListaRachunkow.addComponent(new Label(nadawcaString));

                                /**
                                 * Tabela która zostanie dodana do tego okna
                                 */
                                ArrayList<Rachunek> listaRachunkowNadawcy = listaKlientow.getListaWszystkichKlientow().get(nadawcaID).getListaRachunkow();
                                int rozmiarListyRachunkowNadawcy = listaRachunkowNadawcy.size();
                                Table tableContentNadawca = new Table(rozmiarListyRachunkowNadawcy);
                                if (rozmiarListyRachunkowNadawcy == 0) {
                                    Component[] pustePole = new Component[1];
                                    pustePole[0] = new Label("Ten klient nie posiada żadnego rachunku");
                                    tableContentNadawca.addRow(pustePole);
                                }
                                else {
                                    for (int i = 0 ;i < rozmiarListyRachunkowNadawcy ; i++) {
                                        Component[] rzedyListyRachunkow = new Component[1];
                                        rzedyListyRachunkow[0] = new Label((i+1) + ". "+ listaRachunkowNadawcy.get(i).getNazwaRachunku() + " Saldo : " + listaRachunkowNadawcy.get(i).getSaldo()) ;
                                        tableContentNadawca.addRow(rzedyListyRachunkow);
                                    }
                                }
                                pos10ListaRachunkow.addComponent(tableContentNadawca);
                                pos10ListaRachunkow.addComponent(new EmptySpace());

                                pos10ListaRachunkow.addComponent(new Label(odbiorcaString));

                                /**
                                 * Tabela która zostanie dodana do tego okna
                                 */
                                ArrayList<Rachunek> listaRachunkowOdbiorcy = listaKlientow.getListaWszystkichKlientow().get(odbiorcaID).getListaRachunkow();
                                int rozmiarListyRachunkowOdbiorcy = listaRachunkowOdbiorcy.size();
                                Table tableContentOdbiorca = new Table(rozmiarListyRachunkowOdbiorcy);
                                if (rozmiarListyRachunkowOdbiorcy == 0) {
                                    Component[] pustePole = new Component[1];
                                    pustePole[0] = new Label("Ten klient nie posiada żadnego rachunku");
                                    tableContentOdbiorca.addRow(pustePole);
                                }
                                else {
                                    for (int i = 0 ;i < rozmiarListyRachunkowOdbiorcy ; i++) {
                                        Component[] rzedyListyRachunkow = new Component[1];
                                        rzedyListyRachunkow[0] = new Label((i+1) + ". "+ listaRachunkowOdbiorcy.get(i).getNazwaRachunku() + " Saldo : " + listaRachunkowOdbiorcy.get(i).getSaldo()) ;
                                        tableContentOdbiorca.addRow(rzedyListyRachunkow);
                                    }
                                }


                                /**
                                 * Dodanie tej tabeli do okna
                                 */
                                pos10ListaRachunkow.addComponent(tableContentOdbiorca);

                                pos10ListaRachunkow.addComponent(new EmptySpace());


                                Component[] podajNumerRachunkuNadawy = new Component[1];
                                podajNumerRachunkuNadawy[0] = new Label("Podaj numer rachunku nadawcy");
                                pos10ListaRachunkow.addComponent(podajNumerRachunkuNadawy[0]);

                                TextBox tbPodajNumerRachunkuNadawcy = new TextBox();
                                pos10ListaRachunkow.addComponent(tbPodajNumerRachunkuNadawcy);

                                pos10ListaRachunkow.addComponent(new EmptySpace());

                                Component[] podajNumerRachunkuOdbiorcy = new Component[1];
                                podajNumerRachunkuOdbiorcy[0] = new Label("Podaj numer rachunku odbiorcy");
                                pos10ListaRachunkow.addComponent(podajNumerRachunkuOdbiorcy[0]);

                                TextBox tbPodajNumerRachunkuOdbiorcy = new TextBox();
                                pos10ListaRachunkow.addComponent(tbPodajNumerRachunkuOdbiorcy);

                                pos10ListaRachunkow.addComponent(new EmptySpace());
                                pos10ListaRachunkow.addComponent(new Label("Podaj kwotę przelewu"));
                                TextBox tbKwota = new TextBox();
                                pos10ListaRachunkow.addComponent(tbKwota);


                                /**
                                 * Button Zamknij okno klientów
                                 */
                                Button pos10ListaRachunkowExit = new Button("Zleć przelew", new Action() {
                                    @Override
                                    public void doAction() {
                                        if (isNumeric(tbPodajNumerRachunkuNadawcy.getText()) && isNumeric(tbPodajNumerRachunkuOdbiorcy.getText()) && isNumeric(tbKwota.getText())) {
                                            int idRachunkuNadawcy = Integer.parseInt(tbPodajNumerRachunkuNadawcy.getText()) - 1;
                                            int numberOfAccountsNadawca = listaKlientow.getListaWszystkichKlientow().get(nadawcaID).getListaRachunkow().size();

                                            int idRachunkuOdbiorcy = Integer.parseInt(tbPodajNumerRachunkuOdbiorcy.getText()) - 1;
                                            int numberOfAccountsOdbiorca = listaKlientow.getListaWszystkichKlientow().get(odbiorcaID).getListaRachunkow().size();

                                            int kwota = Integer.parseInt(tbKwota.getText());

                                            String coZwrocilo = "";
                                            if (idRachunkuNadawcy < numberOfAccountsNadawca && idRachunkuOdbiorcy < numberOfAccountsOdbiorca) {
                                                listaKlientow.wykonajPrzelew(nadawcaID, idRachunkuNadawcy, odbiorcaID, idRachunkuOdbiorcy, kwota);

                                            }
                                            else {

                                            }

                                            windowPos10.close();
                                            pos10ListaRachunkow.close();
                                        }

                                    }
                                });

                                pos10ReturnBW.setLastButton(pos10ListaRachunkowExit);
                                pos10ReturnBW.setLastWindow(pos10ListaRachunkow);

                                pos10ListaRachunkowExit.setAlignment(Component.Alignment.RIGHT_CENTER);
                                pos10ListaRachunkow.addComponent(pos10ListaRachunkowExit);
                                guiScreen.showWindow(pos10ListaRachunkow);
                            }
                        }

                    }
                });

                pos10ListaButton.setAlignment(Component.Alignment.RIGHT_CENTER);
                windowPos10.addComponent(pos10ListaButton);

                guiScreen.showWindow(windowPos10);
            }
        });

        pos10ReturnBW.setFirstWindow(windowPos10);
        pos10ReturnBW.setFirstButton(buttonPos10DisplayAccounts);


        buttonPos10DisplayAccounts.setAlignment(Component.Alignment.LEFT_CENTER);

        return pos10ReturnBW;
    }

    public static ButtonWindow pos11WindowAndItsContent(GUIScreen guiScreen, ListaKlientow listaKlientow) {
        Window windowPos11 = windowCreteSubWindow("Odczytaj/zapisz dane do pliku");
        ButtonWindow pos11ReturnBW = new ButtonWindow();

        Button buttonPos11SaveLoad = new Button("11. Odczytaj/zapisz dane do pliku", new Action() {
            @Override
            public void doAction() {
                windowPos11.removeAllComponents();


                windowPos11.addComponent(new Label("Wybierz operację :"));
                windowPos11.addComponent(new Label("1 - Zapisz dane klientów do pliku"));
                windowPos11.addComponent(new Label("2 - Załaduj dane klientów z pliku"));
                TextBox tbZapiszCzyOdczytaj = new TextBox();
                windowPos11.addComponent(tbZapiszCzyOdczytaj);



                Button buttonZapiszOdczytaj = new Button("Wykonaj operację", new Action() {
                    @Override
                    public void doAction() {
                        if (isNumeric(tbZapiszCzyOdczytaj.getText())) {
                            int opcja = Integer.parseInt(tbZapiszCzyOdczytaj.getText());
                            if (opcja == 1) {
                                try {
                                    listaKlientow.zapiszDaneDoPliku();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if (opcja == 2) {
                                try {
                                    listaKlientow.setListaWszystkichKlientow(listaKlientow.wczytajDaneZPliku());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            windowPos11.close();
                        }

                    }
                });
                windowPos11.addComponent(buttonZapiszOdczytaj);
                guiScreen.showWindow(windowPos11);
            }
        });




        pos11ReturnBW.setFirstButton(buttonPos11SaveLoad);
        return pos11ReturnBW;
    }


    /**
     * Autor metody: https://www.freecodecamp.org/news/java-string-to-int-how-to-convert-a-string-to-an-integer/
     */
    private static boolean isNumeric(String str){
        return str != null && str.matches("[0-9.]+");
    }

    private static Table tableASCIIComponent(int id) {
        String[] dolar = {" |.============[_F_E_D_E_R_A_L___R_E_S_E_R_V_E___N_O_T_E_]=============.|",
                " ||%&%&%&%_    _        _ _ _   _ _  _ _ _     _       _    _  %&%&%&%&||",
                " ||%&.-.&/||_||_ | ||\\||||_| \\ (_ ||\\||_(_  /\\|_ |\\|V||_|)|/ |\\ %&.-.&&||",
                " ||&// |\\ || ||_ \\_/| ||||_|_/ ,_)|||||_,_) \\/|  ||| ||_|\\|\\_|| &// |\\%||",
                " ||| | | |%               ,-----,-'____'-,-----,               %| | | |||",
                " ||| | | |&% \"\"\"\"\"\"\"\"\"\"  [    .-;\"`___ `\";-.    ]             &%| | | |||",
                " ||&\\===//                `).'' .'`_.- `. '.'.(`  A 76355942 J  \\\\===/&||",
                " ||&%'-'%/1                // .' /`     \\    \\\\                  \\%'-'%||",
                " ||%&%&%/`   d8888b       // /   \\  _  _;,    \\\\      .-\"\"\"-.  1 `&%&%%||",
                " ||&%&%&    8P |) Yb     ;; (     > a  a| \\    ;;    //A`Y A\\\\    &%&%&||",
                " ||&%&%|    8b |) d8     || (    ,\\   \\ |  )   ||    ||.-'-.||    |%&%&||",
                " ||%&%&|     Y8888P      ||  '--'/`  -- /-'    ||    \\\\_/~\\_//    |&%&%||",
                " ||%&%&|                 ||     |\\`-.__/       ||     '-...-'     |&%&%||",
                " ||%%%%|                 ||    /` |._ .|-.     ||                 |%&%&||",
                " ||%&%&|  A 76355942 J  /;\\ _.'   \\  } \\  '-.  /;\\                |%&%&||",
                " ||&%.-;               (,  '.      \\  } `\\   \\'  ,)   ,.,.,.,.,   ;-.%&||",
                " ||%( | ) 1  \"\"\"\"\"\"\"   _( \\  ;...---------.;.; / )_ ```\"\"\"\"\"\"\" 1 ( | )%||",
                " ||&%'-'==================\\`------------------`/=================='-'%&||",
                " ||%&JGS&%&%&%&%%&%&&&%&%%&)O N E  D O L L A R(%&%&%&%&%&%&%%&%&&&%&%%&||"};
        String[] euro = {"           _.-------._",
                "        _-'_.------._ `-_",
                "      _- _-          `-_/",
                "     -  -",
                " ___/  /______________",
                "/___  .______________/",
                " ___| |_____________",
                "/___  .____________/",
                "    \\  \\",
                "     -_ -_             /|",
                "       -_ -._        _- |",
                "         -._ `------'_./",
                "            `-------'"};

        Table returnTable = new Table();
        Component[] emptySpace = new Component[1];
        emptySpace[0] = new Label("");
        /*
        for (int j = 0 ; j < 3 ; j++) {
            returnTable.addRow(emptySpace[0]);
        }

         */
        if (id == 1) {
            for (int i = 0; i < euro.length ; i++) {
                Component[] euroComponent = new Component[1];
                euroComponent[0] = new Label(euro[i]);
                returnTable.addRow(euroComponent[0]);
            }
        }
        else {
            for (int i = 0; i < dolar.length ; i++) {
                Component[] dolarComponent = new Component[1];
                dolarComponent[0] = new Label(dolar[i]);
                returnTable.addRow(dolarComponent[0]);
            }
        }
        return returnTable;
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {


        ListaKlientow listaKlientow = new ListaKlientow();
        populateData(listaKlientow);

        FrontEndTextMenu(listaKlientow);


    }
public static void populateData(ListaKlientow listaKlientow) {

    Klient klient1 = new Klient("Jan", "Kowalski");
    listaKlientow.dodajKlientaDoListy(klient1);

    klient1.getListaRachunkow().get(0).dodajWpisHistoriiTransakcji(klient1.getListaRachunkow().get(0).zwiekszSaldo(9000).getWiadomosc()) ;
    klient1.getListaRachunkow().get(0).dodajWpisHistoriiTransakcji(klient1.getListaRachunkow().get(0).zmniejszSaldo(8000).getWiadomosc());

    klient1.dodajRachunekOszczednosciowy();
    klient1.getListaRachunkow().get(1).dodajWpisHistoriiTransakcji(klient1.getListaRachunkow().get(1).zwiekszSaldo(10000).getWiadomosc());
    klient1.getListaRachunkow().get(1).dodajWpisHistoriiTransakcji(klient1.getListaRachunkow().get(1).zmniejszSaldo(5000).getWiadomosc());
    klient1.getListaRachunkow().get(1).dodajWpisHistoriiTransakcji(klient1.getListaRachunkow().get(1).zmniejszSaldo(3000).getWiadomosc());

    Klient klient2 = new Klient("Marek", "Nowak");
    listaKlientow.dodajKlientaDoListy(klient2);
    klient2.dodajRachunekOszczednosciowy("Spory Rachunek oszczędnościowy");
    klient2.dodajRachunekZwykly("Zwykły rachunek na niezwykłe wydatki");
    klient2.getListaRachunkow().get(0).dodajWpisHistoriiTransakcji(klient2.getListaRachunkow().get(0).zwiekszSaldo(100000).getWiadomosc());
    klient2.getListaRachunkow().get(0).dodajWpisHistoriiTransakcji(klient2.getListaRachunkow().get(0).zmniejszSaldo(1000).getWiadomosc());

    klient2.getListaRachunkow().get(1).dodajWpisHistoriiTransakcji(klient2.getListaRachunkow().get(1).zwiekszSaldo(70000).getWiadomosc());
    klient2.getListaRachunkow().get(1).dodajWpisHistoriiTransakcji(klient2.getListaRachunkow().get(1).zmniejszSaldo(700).getWiadomosc());



    listaKlientow.wykonajPrzelew(0, 1, 1, 0, 500);
    /**
     * Koniec ladowania danych
     */
}

}
