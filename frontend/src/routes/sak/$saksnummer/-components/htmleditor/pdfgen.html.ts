export const html= `<!DOCTYPE html>
<html lang="no">

<head>
    <meta charset="UTF-8"/>
    <meta name="description" content="Vedtaksbrev for Helsetjenester"/>
    <meta name="author" content="Team historisk"/>
    <meta name="subject" content="Helsetjenester"/>
    <style>
        * {
            font-family: "Source Sans Pro", SourceSansPro, Source_Sans_Pro, ArialSystem, sans-serif;
            margin-left: 0;
            margin-right: 0;
        }

        @page {
            size: A4 portrait;
            margin: 64px 64px 64px 74px;
            font-family: "Source Sans Pro", SourceSansPro, Source_Sans_Pro, ArialSystem, sans-serif;

            @bottom-left {
                font-size: 10px;
                content: "Behandlingsnummer: 123123123";
                font-family: "Source Sans Pro", SourceSansPro, Source_Sans_Pro, ArialSystem, sans-serif;
            }
        }

        @page {
            @bottom-right {
                font-size: 10px;
                content: "side "counter(page) " av "counter(pages);
                font-family: "Source Sans Pro", SourceSansPro, Source_Sans_Pro, ArialSystem, sans-serif;
            }

            @top-right {
                font-size: 1.5rem;
                font-weight: bold;
                color: red;
                content: "";
                font-family: "Source Sans Pro", SourceSansPro, Source_Sans_Pro, ArialSystem, sans-serif;
            }
        }

        #pagenumber:before {
            content: counter(page);
        }

        #pagecount:before {
            content: counter(pages)
        }

        header {
            height: 48px;
            position: relative;
        }

        .navLogo {
            height: 16px;
            margin-bottom: 48px;
        }

        .personalia {
            margin-right: 10%;
            float: left;
        }

        .personalia p{
            margin: 0;
        }

        .dato {
            position: absolute;
            bottom: 0;
            right: 0;
            margin: 0;
        }

        /*
        ** Styling under her følger aksel sine anbefalinger
        ** https://aksel.nav.no/monster-maler/brev/visuelle-retningslinjer-for-brev
        */

        h1 {
            margin-top: 40px;
            margin-bottom: 26px;
            font-size: 16px;
            line-height: 20px;
            font-weight: bold;
        }

        h2 {
            font-size: 13px;
        }

        h3 {
            font-size: 12px;
        }

        h4 {
            font-size: 11px;
        }

        h2,
        h3,
        h4 {
            margin-top: 26px;
            line-height: 16px;
            margin-bottom: 6px;
            font-weight: bold;
        }

        p, ul{
            font-size: 12px;
            line-height: 16px;
            margin-top: 0;
        }


        ul {
            padding-left:16px;
            margin-bottom: 16px;
        }
        ul p {
            margin: 0
        }

        .hilsen {
            margin-top: 32px;
        }

        .hilsen p{
            margin: 0;
        }

        .signatur {
            margin-top: 26px;
        }

        .beslutter {
            float: left;
            margin-right: 50%;
        }

        .venstrestill {
            float: left;
        }

        .right-align {
            text-align: right;
        }

        a {
            text-decoration: none;
        }

        .forhandsvisning {
            position: absolute;
            top: 10px;
            right: 10px;
            color: red;
            font-size: 1.5rem;
            font-weight: bold;
        }
    </style>

    <title>Vedtaksbrev for Helsetjenester</title>
</head>
<body>
<article>
    <section>
        <img alt="Nav logo" class="navLogo" src="data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHN2ZyBpZD0iTGF5ZXJfMSIgZGF0YS1uYW1lPSJMYXllciAxIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZlcnNpb249IjEuMSIgdmlld0JveD0iMzIwIDI2OSAyMDAgNTgiPgogIDxkZWZzPgogICAgPHN0eWxlPgogICAgICAuY2xzLTEgewogICAgICAgIGZpbGw6ICNjMzAwMDA7CiAgICAgICAgZmlsbC1ydWxlOiBldmVub2RkOwogICAgICAgIHN0cm9rZS13aWR0aDogMHB4OwogICAgICB9CiAgICA8L3N0eWxlPgogIDwvZGVmcz4KICA8cGF0aCBjbGFzcz0iY2xzLTEiIGQ9Ik01MTQuOCwyNjkuOGgtMTkuOXMtMS40LDAtMS45LDEuMmwtMTEsMzMuOC0xMS0zMy44Yy0uNS0xLjItMS45LTEuMi0xLjktMS4yaC0zOC4zYy0uOCwwLTEuNS43LTEuNSwxLjV2MTEuNWMwLTkuMS05LjctMTMtMTUuMy0xMy0xMi43LDAtMjEuMiw4LjQtMjMuOCwyMS4xLS4xLTguNC0uOC0xMS41LTMuMS0xNC41LTEtMS41LTIuNS0yLjgtNC4yLTMuOC0zLjQtMi02LjQtMi43LTEyLjktMi43aC03LjdzLTEuNCwwLTEuOSwxLjJsLTcsMTcuM3YtMTdjMC0uOC0uNy0xLjUtMS41LTEuNWgtMTcuN3MtMS40LDAtMS45LDEuMmwtNy4yLDE4cy0uNywxLjguOSwxLjhoNi44djM0LjJjMCwuOS43LDEuNSwxLjUsMS41aDE3LjZjLjgsMCwxLjUtLjcsMS41LTEuNXYtMzQuMmg2LjljMy45LDAsNC44LjEsNi4zLjguOS40LDEuOCwxLDIuMiwxLjkuOSwxLjcsMS4yLDMuOCwxLjIsMTB2MjEuNWMwLC45LjcsMS41LDEuNSwxLjVoMTYuOHMxLjksMCwyLjYtMS45bDMuNy05LjJjNSw3LDEzLjEsMTEuMSwyMy4zLDExLjFoMi4yczEuOSwwLDIuNy0xLjlsNi41LTE2LjF2MTYuNWMwLC45LjcsMS41LDEuNSwxLjVoMTcuMnMxLjksMCwyLjctMS45YzAsMCw2LjktMTcuMSw2LjktMTcuMmgwYy4zLTEuNC0xLjUtMS40LTEuNS0xLjRoLTYuMXYtMjkuM2wxOS4zLDQ3LjljLjgsMS45LDIuNiwxLjksMi42LDEuOWgyMC4zczEuOSwwLDIuNy0xLjlsMjEuNC01M2MuNy0xLjgtMS40LTEuOC0xLjQtMS44aDBaTTQyOS4yLDMwNmgtMTEuNWMtNC42LDAtOC4zLTMuNy04LjMtOC4zczMuNy04LjMsOC4zLTguM2gzLjJjNC42LDAsOC4zLDMuNyw4LjMsOC4zdjguM2gwWiIvPgo8L3N2Zz4&#x3D;"/>

        <header>
            <div class="personalia">
                <p>Navn: </p>
                <p>Fødselsnummer: </p>
                <p>Behandlingsnummer: </p>
            </div>
            <div class="personalia inline">
                <p>Planet Planetus</p>
                <p>12345678901</p>
                <p>123123123</p>
            </div>
            <p class="dato">3. august 2023</p>
        </header>


    </section>
    <section>

        <div class="data-editable">
            <h1>h1 heading</h1>
            <h2>h2 heading</h2>
            <h3>h3 heading</h3>
            <h4>h4 heading</h4>


            <b>bold</b>
            <i>italic</i>

            <p>lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget consectetur sagittis, nisl nunc
                egestas nunc, vitae facilisis enim nisi nec nisi. Sed euismod, nisl eget consectetur sagittis, nisl nunc egestas
                nunc, vitae facilisis enim nisi nec nisi.</p>

            <ul>
                <li>List item 1</li>
                <li>List item 2</li>
                <li>List item 3</li>
            </ul>

            <ol>
                <li>List item 1</li>
                <li>List item 2</li>
                <li>List item 3</li>
                <li>List item 4</li>
            </ol>

            <table>
                <caption>
                    Council budget (in £) 2018
                </caption>
                <thead>
                <tr>
                    <th>Items</th>
                    <th>Expenditure</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <th>Donuts</th>
                    <td>3,000</td>
                </tr>
                <tr>
                    <th >Stationery</th>
                    <td>18,000</td>
                </tr>
                </tbody>
                <tfoot>
                <tr>
                    <th >Totals</th>
                    <td>21,000</td>
                </tr>
                </tfoot>
            </table>
        </div>
        <!-- Innholdet i partial-block er brev.hbs -->
    </section>


    <section>
        <h2>Du kan klage på vedtaket</h2>
        <p>
            Hvis du mener vedtaket er feil, kan du klage innen 6 uker fra den datoen vedtaket har kommet fram til deg.
            Dette følger av folketrygdloven § 21-12. Du finner skjema og informasjon på nav.no/klage.
        </p>

        <p>Nav kan veilede deg på telefon om hvordan du sender en klage. Nav-kontoret ditt kan også hjelpe deg med å skrive en klage.
            Kontakt oss på telefon 55 55 33 33/55 55 33 34 hvis du trenger hjelp.
        </p>

        <p>
            Hvis du får medhold i klagen, kan du få dekket vesentlige utgifter som har vært nødvendige for å få endret vedtaket, for eksempel hjelp fra advokat.
            Du kan ha krav på fri rettshjelp etter rettshjelploven. Du kan få mer informasjon om denne ordningen hos advokater, statsforvalteren eller Nav.
        </p>

        <p>Du kan lese om saksomkostninger i forvaltningsloven § 36.</p>
        <p>Hvis du sender klage i posten, må du signere klagen.</p>
        <p>Mer informasjon om klagerettigheter finner du på nav.no/klagerettigheter.</p>
    </section>


    <section>
        <h2>Du har rett til innsyn i saken din</h2>
        <p>
            Du har rett til å se dokumentene i saken din. Dette følger av forvaltningsloven § 18.
            Kontakt oss om du vil se dokumentene i saken din. Ta kontakt på nav.no/kontakt eller på telefon 55 55 33 33/55 55 33 34.
            Du kan lese mer om innsynsretten på nav.no/personvernerklaering.
        </p>
    </section>

    <section>
        <h2>Du har rettigheter knyttet til personopplysningene dine</h2>
        <p>
            Du finner informasjon om hvordan Nav behandler personopplysningene dine, og hvilke rettigheter du har, på
            nav.no/personvernerklaering#hvordan.
        </p>
        <p>
            Nav kan veilede deg på telefon 55 55 33 33/55 55 33 34 om hvordan Nav behandler personopplysninger.
        </p>
    </section>
    <section>
        <h2>Du har rett til å få veiledning fra Nav</h2>
        <p>
            Vi har plikt til å veilede deg om dine rettigheter og plikter i saken din, både før, under og etter saksbehandlingen.
            Dette følger av forvaltningsloven § 11. Ta kontakt på telefon 55 55 33 33/55 55 33 34 eller på nav.no/kontakt hvis du har spørsmål.
        </p>
    </section>
    <section>
        <h2>Har du spørsmål? </h2>
        <p>
            Du finner mer informasjon om ortopediske hjelpemidler på nav.no/protese-ortose-ortopediskesko-parykk.
            På nav.no/kontakt kan du chatte eller skrive til oss.
            Hvis du ikke finner svar på nav.no kan du ringe oss på telefon 55 55 33 33/55 55 33 34, hverdager 09.00-15.00.
        </p>
    </section>

    <section class="hilsen">
        <p>Med vennlig hilsen</p>
        <p>Nav Hjelpemidler Øst Viken</p>
        <div class="signatur">
            <div class="beslutter">

                <p>Pelle Parafin</p>

            </div>
            <div>
                <p>Kåre Kropp</p>
            </div>
        </div>
    </section>
</article>
</body>
</html>
 `