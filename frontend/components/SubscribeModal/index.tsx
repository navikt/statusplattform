import React from 'react'
import styled from 'styled-components'

import { Telephone, Email, List } from '@navikt/ds-icons'
import { Input } from 'nav-frontend-skjema';
import { Hovedknapp } from 'nav-frontend-knapper';
import Lukknapp from 'nav-frontend-lukknapp';
import { ISource, SourceType } from 'types/source';


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
    background-color: var(--navGra40);
    height: 3rem;
    max-width: 6rem;
    //Use !important to override nav-Telephone and nav-Email styles
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    border: 1px solid black;
    :hover {
        cursor: pointer;
        background-color: var(--navBlaLighten40);
    }
    img {
        min-height: 3rem;
    }
    :active {
        background: var(--navGra80);
    }
`

const SubscribeModalContent = styled.div`
    padding: 1rem;
    display: flex;
    flex-direction: column;
    p {
        margin-top: 0;
    }
    div {
        display: flex;
        flex-direction: column;
        :first-child {
            margin-bottom: 10px;
        }
    }
`

const CustomLukknapp = styled(Lukknapp)`
    border: none;
    color: var(--navBakgrunn);
    :hover {
        background-color: transparent;
    }
`



const handleSlack = (e) => {
    e.preventDefault();
    alert("ikke implementert")
}


const subscribeSources: ISource[] = [
    {
        id: SourceType.phone,
        title: "Telefon",
        content: <Telephone/>,
        text: "Du kan motta sms-varsler når statusmeldinger oppdateres. Dersom du ønsker dette, fyll inn nummeret nedenfor: ",
    },{
        id: SourceType.email,
        title: "Epost",
        content: <Email/>,
        text: "Ved oppdaterte statusmeldinger kan du motta varsling på email. Fyll inn epost nedenfor om dette er ønskelig",
    },{
        id: SourceType.slack,
        title: "Slack",
        content: <img src="/assets/images/slack-icon.svg" alt="Slack icon"/>,
        text: "Du kan få statusmeldinger rett i Slack. Trykk nedenfor for å starte abonnering",
    },{
        id: SourceType.close,
        title: "Close",
        content: <CustomLukknapp />,
        text: "Skal emitte en lukk-kommando. Ikke ferdig implementert"
    }
]


const SubscribeModal: React.FC = () => {

    const [currentActiveSource, setActiveSource] = React.useState<ISource>(subscribeSources[0])

    const handleSubmit = (id, e) => {
        e.preventDefault()
        alert("ikke implementert")
    }

    

    return (
        <SubscribeModalContainer>
            <ul>
                {
                    subscribeSources.map((source) => (
                        <ListItemWrapper key={source.id} id={source.id} onClick={(e) => setActiveSource(source)}>{source.content} </ListItemWrapper>
                    ))
                }
            </ul>
            <SubscribeModalContent>
                {currentActiveSource.id === SourceType.slack ? 
                    (
                        <>
                            <form>
                                <p>{currentActiveSource.text}</p>
                                <Hovedknapp onClick={(e) => handleSlack(e)}>Abonner</Hovedknapp>
                            </form>
                        </>
                    ) :
                    (
                        <>
                            <p>{currentActiveSource.text}</p>
                            <form>
                                <Input label={currentActiveSource.title}></Input>
                                <Hovedknapp onClick={(e) => handleSubmit(currentActiveSource.id, e)}>Abonner</Hovedknapp>
                            </form>
                        </>
                    )
                }

            </SubscribeModalContent>
        </SubscribeModalContainer>
    )
}

export default SubscribeModal