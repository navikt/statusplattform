import Head from 'next/head'
import styled from 'styled-components'

import '../styles/Home.css'
import FetchNavDigitalServices from './FetchNavDigitalServices'


const PortalDigitaleTjenesterContainer = styled.div`
    min-height: 100vh;
    margin-bottom: -100px;
    display: flex;
    flex-direction: column;
`;

const Header = styled.header`
    display: flex;
    justify-content: flex-start;
    align-items: center;
    padding-left: 20px;
    height: 100%;

    img {
        width: 84px;
    }
`;

const MainContent = styled.div`
    display: flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    margin-top: 3rem;
    margin-bottom: 3rem;
    color: #0067C5;
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
         width: 63px;
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
        <PortalDigitaleTjenesterContainer>
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
        </PortalDigitaleTjenesterContainer>
    )
}

