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
    border-top: 1px solid #eaeaea;
    padding: 1rem;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    

    > ul {
        padding: 0;
        display: flex;
        flex-direction: column;
        list-style: none;
    }

    img {
        width: 63px;
        :hover {
            transform: scale(1.05)
        }
    }

    a {
        color: #0067c5;
        background: none;
        text-decoration: underline;
        margin: 20px;
        :hover {
            text-decoration: none;
        }
    }

    @media (min-width: 700px) {
        flex-flow: row;
        justify-content: center;
        align-items: center;
        > ul {
            display: flex;
            flex-direction: row;
            padding: 0;
        }
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
                <a href="https://www.nav.no/no/person#">
                    <img src="/assets/nav-logo/png/black.png" alt="LogoBlack" ></img>
                </a>
                <p>Arbeids- og velferdsetaten</p>
                <ul>
                    <a href="https://www.nav.no/no/nav-og-samfunn/om-nav/personvern-i-arbeids-og-velferdsetaten">Personvern og informasjonskapsler</a>
                    <a href="https://www.nav.no/no/nav-og-samfunn/kontakt-nav/teknisk-brukerstotte/nyttig-a-vite/tilgjengelighet">Tilgjengelighet</a>
                    <a href="https://www.nav.no/no/person#">Del skjerm med veileder</a>
                </ul>
            </Footer>
        </PortalDigitaleTjenesterContainer>
    )
}

