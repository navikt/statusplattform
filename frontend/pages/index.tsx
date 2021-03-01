import Head from 'next/head'
import Panel from 'nav-frontend-paneler';
// import Lenkepanel from 'nav-frontend-lenkepanel';
import styled from 'styled-components'

// import '../styles/Home.css'
import FetchNavDigitalServices from './FetchNavDigitalServices'



const Header = styled.header`
    display: flex;
    /* justify-content: space-around; */
    justify-content: flex-start;
    align-items: center;
    padding: 20px;

    img {
        width: 63px;
        height: 100%;
    }
`;

const MainContent = styled.div`
    display: flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    margin-top: 3rem;
    margin-bottom: 3rem;
`;

const Footer = styled.footer`
    width: 100%;
    margin-top: auto; /*Footer always at bottom (if min.height of container is 100vh)*/
    height: 100px;
    border-top: 1px solid #eaeaea;
    display: flex;
    justify-content: center;
    align-items: center;

    img {
         width: 90px;
    }

    a {
        color: #0067c5;
        background: none;
        text-decoration: underline;
        cursor: pointer;
        margin: 20px;
    }
`;

export default function Home() {
    return (
        <div className="container">
            <Head>
                <title>Status digitale tjenester</title>
                <link rel="icon" href="/favicon.ico" />
                <meta name="viewport" content="initial-scale=1.0, width=device-width" />
            </Head>

            <Header>
                <img src="/assets/nav-logo/png/red.png" alt="LogoRed" />
                <h1>
                    Status digitale tjenester
                </h1>
            </Header>
            <main>
                <MainContent>
                    <FetchNavDigitalServices />
                </MainContent>
            </main>

            <Footer>
                <img src="/assets/nav-logo/png/black.png" alt="LogoBlack" />
                <ul>
                    <p>Arbeids- og velferdsetaten</p>
                    <a href="https://www.nav.no/no/nav-og-samfunn/om-nav/personvern-i-arbeids-og-velferdsetaten">Personvern og informasjonskapsler</a>
                    <a href="https://www.nav.no/no/nav-og-samfunn/kontakt-nav/teknisk-brukerstotte/nyttig-a-vite/tilgjengelighet">Tilgjengelighet</a>
                    <a href="https://www.nav.no/no/person#">Del skjerm med veileder</a>
                </ul>
            </Footer>
        </div>
    )
}

