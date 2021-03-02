import { useEffect, useState } from "react";
import Panel from 'nav-frontend-paneler';
import styled from 'styled-components'

import { Bag, Folder, PensionBag, HealthCase, SuccessFilled, ErrorFilled, WarningFilled, Employer, Information, People, Family } from '@navikt/ds-icons'
import { Undertittel } from "nav-frontend-typografi";

const DigitalServicesContainer = styled.div`
    flex: 1;
    width: 90%;
    padding: 30px;
    display: grid;
    grid-template-columns: repeat(1, 1fr);
    gap: 15px;
    @media (min-width: 768px) {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
    }
`;

const PanelSetWidth = styled(Panel)`
    /* width: 250px; */
    margin: 10px;
    color: #0067C5;
    > div {
        /* width: 180px; */
        height: 100%;
        padding-bottom: 16px;
        
        
        h2 svg:first-child {
            width: 1.778rem;
            height: 1.778rem;
        }
    }
`;

const ExpandDataButton = styled.button`
    color: #0067C5;
    width: 100px;
    height: 25px;
    border-color: #0067C5;
    border-width: 1px;
    border-radius: 5px;
    outline: none;
    &:active {
        background-color: white;
        border-color: #0067C5;
    }
`;

const HorizontalLine = styled.div`
    width: 100%;
    background-color: #0067C5;
    height: 1px;
`;

const ErrorParagraph = styled.p`
    color: #ff4a4a;
    /* background-color: grey; */
    font-weight: bold;
    padding: 10px;
    border-radius: 5px;
`;

const SuccessFilledColored = styled(SuccessFilled)`
    color: green;
`;

const ErrorFilledColored = styled(ErrorFilled)`
    color: #d60000;
`;

const WarningFilledColored = styled(WarningFilled)`
    color: #ff9900;
`;

const ServicesList = styled.ul`
    padding: 0;
    > li {
        display: flex;
        justify-content: space-between;
        align-items: center;
        /* border: 1px solid; */
        margin: 5px;
        list-style-type: none;
        section:first-child {
            width: 90%;
            white-space: normal;
            word-wrap: break-word;
        }
    }
`;


async function fetchData() {
    try {
        const response = await fetch("http://localhost:3001/rest/testAreas");
        if (response.ok) {
            const data = await response.json()
            return data
        }
        else {
            throw Error(`Request rejected with error code:  + ${response.status}`)
        }
    } catch (error) {
        console.error("Error is: " + error)
    }
}

const handleAndSetNavIcon = (areaName: string) => {
    if (areaName == "Arbeid") {
        return <Bag />
    }
    if (areaName == "Pensjon") {
        return <PensionBag />
    }
    if (areaName == "Helse") {
        return <HealthCase />
    }
    if (areaName == "Ansatt") {
        return <Employer />
    }
    if (areaName == "Informasjon") {
        return <Information />
    }
    if (areaName == "Person") {
        return <People />
    }
    if (areaName == "Familie") {
        return <Family />
    }
    return <Folder />
}

const handleAndSetStatusIcon = (status: string) => {
    if (status == "OK") {
        return <SuccessFilledColored />
    }
    if (status == "DOWN") {
        return <ErrorFilledColored />
    }
    if (status == "ISSUE") {
        return <WarningFilledColored />
    }
    return status
}

function FetchNavDigitalServices() {
    const [areas, setAreas] = useState([])
    const [isLoading, setIsLoading] = useState(false)

    useEffect(() => {
        (async function () {
            setIsLoading(true)
            const newAreas = await fetchData()
            setAreas(newAreas)
            setIsLoading(false)
        })()
    }, [])

    if (!areas) {
        return <ErrorParagraph>Kunne ikke hente de digitale tjenestene. Hvis problemet vedvarer, kontakt support.</ErrorParagraph>
    }

    if (isLoading) {
        return <p>Loading services...</p>
    }

    return (
        <DigitalServicesContainer>
            {areas.map(area => (
                <PanelSetWidth border key={area.name}>
                    <div>
                        <Undertittel>
                            {handleAndSetNavIcon(area.name)}
                            {" " + area.name}
                        </Undertittel>
                        <HorizontalLine />
                        <ServicesList>
                            {area.services.map(service => (
                                <li key={service.name}>
                                    <section>{service.name}</section><section> {handleAndSetStatusIcon(service.status)}</section>
                                </li>
                            ))}
                        </ServicesList>
                        <ExpandDataButton>Se mer...</ExpandDataButton>
                    </div>
                </PanelSetWidth>
            ))}
        </DigitalServicesContainer>

    )
}

export default FetchNavDigitalServices