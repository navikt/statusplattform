import styled from 'styled-components'
import Head from 'next/head'

import Header from 'components/Header'
import Navbar from 'components/Navbar'
import Footer from 'components/Footer'


const MainContentContainer = styled.div`
    min-height: 100vh;
    margin-bottom: -100px;
    display: flex;
    flex-direction: column;
    background-color: var(--navGraBakgrunn);
    overflow: hidden;
`;

const Content = styled.main`
    width: 100%;
    min-height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-flow: column wrap;
`;


const MainContent = props => {
    return(
        <MainContentContainer>
            <Head>
                <title>Status digitale tjenester</title>
                <link rel="icon" href="/favicon.ico" />
                <meta name="viewport" content="initial-scale=1.0, width=device-width" />
            </Head>
            <Header/>
            <Navbar/>
            <Content>
                {props.children}
            </Content>

            <Footer/>
        </MainContentContainer>
    )
}

export default MainContent