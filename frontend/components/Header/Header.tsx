import styled from 'styled-components'
import React from 'react'

import { Sidetittel } from 'nav-frontend-typografi'
import { Knapp } from 'nav-frontend-knapper';

import SubscribeModal from '../SubscribeModal/SubscribeModal'

const CustomHeader = styled.header`
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
        padding: 0 3rem;
        flex-flow: row nowrap;
        align-items: center;
        justify-content: flex-start;
        > span {
            padding-left: 20px;
        }
        
    }
`
const SidetittelCustomized = styled(Sidetittel)`
    width: 275px;
    @media(min-width: 390px){
        width: 100%;
    }
`
const HeaderContent = styled.span`
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
    @media (min-width: 450px){
        flex-direction: row;
    }
`
const SubscribeButton = styled(Knapp)`
    border-radius: 30px;
    height: 3rem;
`
const SubscribeModalWrapper = styled.div`
    right: 0;
    top: 170px;
    position: absolute;
    @media(min-width: 350px){
        top: 118px;
    }
    @media(min-width: 450px){
        top: 10%;
    }
`


const Header = () => {
    const [subscribeModalHidden, setSubscribeModalBoolean] = React.useState(false)

    const onClickHandler = () => {
        setSubscribeModalBoolean(!subscribeModalHidden)
    }

    return (
        <CustomHeader>
            <img src="/assets/nav-logo/png/red.png" alt="LogoRed" />
            <HeaderContent>
                <SidetittelCustomized>
                    Status digitale tjenester
                </SidetittelCustomized>
                <span>
                    <SubscribeButton mini onClick={onClickHandler}>Abonner</SubscribeButton>
                </span>
                {subscribeModalHidden && 
                    <SubscribeModalWrapper>
                        <SubscribeModal/>
                    </SubscribeModalWrapper>
                }
            </HeaderContent>
        </CustomHeader>
    )
}

export default Header