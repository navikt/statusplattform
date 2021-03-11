import styled from 'styled-components'

import { Sidetittel } from 'nav-frontend-typografi'

const CustomHeader = styled.header`
    padding-left: 20px;
    min-height: 106px;
    height: 100%;
    background-color: white;
    border-bottom: 1px solid #c6c2bf;
    display: flex;
    flex-flow: column wrap;
    img {
        max-width: 84px;
    }
    
    > h1 {
        font-size: 1.875rem;
        font-weight: 600;
    }
    @media (min-width: 350px){
        flex-flow: row nowrap;
        align-items: center;
        justify-content: flex-start;
        > div {
            padding-left: 20px;
        }
        
    }
`;

const Header = () => {
    return (
        <CustomHeader>
            <img src="/assets/nav-logo/png/red.png" alt="LogoRed" />
            <div>
                <Sidetittel>
                    Status digitale tjenester
                </Sidetittel>
            </div>
        </CustomHeader>
    )
}

export default Header