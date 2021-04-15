import styled from 'styled-components'

const FooterCustomized = styled.footer`
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
        color: var(--navBla);
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

const Footer = () => {
    return (
        <FooterCustomized>
            <a href="https://www.nav.no/no/person#">
                <img src="/assets/nav-logo/png/black.png" alt="LogoBlack" ></img>
            </a>
                <p>Arbeids- og velferdsetaten</p>
            <ul>
                <a href="https://www.nav.no/no/nav-og-samfunn/om-nav/personvern-i-arbeids-og-velferdsetaten">Personvern og informasjonskapsler</a>
                <a href="https://www.nav.no/no/nav-og-samfunn/kontakt-nav/teknisk-brukerstotte/nyttig-a-vite/tilgjengelighet">Tilgjengelighet</a>
                <a href="https://www.nav.no/no/person#">Del skjerm med veileder</a>
            </ul>
        </FooterCustomized>
    )
}

export default Footer