import styled from 'styled-components'
import Navbar from './Navbar'
import Head from 'next/head'

import { Sidetittel } from 'nav-frontend-typografi'


const MainContentContainer = styled.div`
    min-height: 100vh;
    margin-bottom: -100px;
    display: flex;
    flex-direction: column;
    background-color: var(--navGraBakgrunn);
`;
const Header = styled.header`
    padding-left: 20px;
    min-height: 106px;
    height: 100%;
    background-color: white;
    border-bottom: 1px solid #c6c2bf;
    display: flex;
    justify-content: flex-start;
    align-items: center;
    flex-wrap: nowrap;
    > div {
        padding-left: 20px;
    }
    > h1 {
        font-size: 1.875rem;
        font-weight: 600;
    }

    img {
        width: 84px;
    }
`;
const Content = styled.main`
    width: 100%;
    min-height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    color: #0067C5;
`;

const Footer = styled.footer`
    width: 100%;
    margin-top: auto; /*Footer always at bottom (if min.height of container is 100vh)*/
    border-top: 1px solid #eaeaea;
    background-color: white;
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

const MainContent = props => {
    return(
        <MainContentContainer>
            <Head>
                <title>Status digitale tjenester</title>
                <link rel="icon" href="/favicon.ico" />
                <meta name="viewport" content="initial-scale=1.0, width=device-width" />
            </Head>
            <Header>
                <img src="/assets/nav-logo/png/red.png" alt="LogoRed" />
                <div>
                    <Sidetittel>
                        Status digitale tjenester
                    </Sidetittel>
                </div>
            </Header>
            <Navbar/>

            <Content>
                {props.children}
            </Content>

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
        </MainContentContainer>
    )
}

export default MainContent