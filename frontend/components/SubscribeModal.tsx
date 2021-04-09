import styled from 'styled-components'

import { Telephone, Email } from '@navikt/ds-icons'


const SubscribeModalContainer = styled.div`
    position: relative;
    background-color: var(--navBakgrunn);
    box-shadow: 1px 1px 4px 5px #ccc;
    max-width: 250px;
    border-bottom-left-radius: 10px;
    border-bottom-right-radius: 10px;
    ul > li {
        list-style: none;
        border: solid rgba(0, 0, 0, 25%);
        border-width: 0 1px;
        width: 100%;
        padding: 0.5rem 1rem;
        display: inline;
    }
    ul {
        padding: 0;
        margin: 0;
        border-bottom: 1px solid #ccc;
        font-size: 1.5rem;
        color: white;
        display: flex;
        justify-content: space-between;
    }
`

const ListItemWrapper = styled.li`
    background-color: grey;
    display: flex;
    justify-content: center;
    align-items: center;
    border: 1px solid black;
    :hover {
        cursor: pointer;
    }
`

const SubscribeModalTextContent = styled.div`
    padding: 1rem;
`

export default function SubscribeModal() {
    return (
        <SubscribeModalContainer>
            <ul>
                <ListItemWrapper><Telephone/></ListItemWrapper>
                <ListItemWrapper><Email/></ListItemWrapper>
                <ListItemWrapper><img src="/assets/images/slack-icon.svg" alt="Slack icon"/></ListItemWrapper>
            </ul>
            <SubscribeModalTextContent>
                Funksjonalitet ikke ferdig enda
            </SubscribeModalTextContent>
        </SubscribeModalContainer>
    )
}