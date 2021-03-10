import { useEffect, useState } from "react";
import {PortalServiceTile} from '../components/PortalServiceTile'

import Panel from 'nav-frontend-paneler';
import styled from 'styled-components'

import { Bag, Folder, PensionBag, HealthCase, ErrorFilled, WarningFilled, Employer, Information, People, Family, Service, Globe } from '@navikt/ds-icons'
import { Undertittel } from "nav-frontend-typografi";

const DigitalServicesContainer = styled.div`
    width: 100%;
    padding: 30px;
    /* flex: 1; */
    display: grid;
    justify-content: center;
    grid-template-columns: repeat(1, 1fr);
    gap: 15px;
    @media (min-width: 768px) {
        display: grid;
        grid-template-columns: repeat(auto-fill, 250px);
    }
`;

const PanelCustomized = styled(Panel)`
    background-color: #bedeca;
    margin: 10px;
    color: #0067C5;
    > div {
        height: 100%;
        padding-bottom: 16px;
        
        h2 svg:first-child {
            width: 1.778rem;
            height: 1.778rem;
        }
    }
`;

const UndertittelCustomized = styled(Undertittel)`
    display: flex;
    align-items: center;
    flex-direction: row;
    svg {
        margin-right: 10px;
    }
`;

const ServicesList = styled.ul`
    padding: 0;
    color:black;
    > li {
        display: flex;
        justify-content: flex-start;
        margin: 5px;
        list-style-type: none;
        section {
            display: flex;
            align-items: center;
        }
        section:first-child {
            margin-right: 10px;
        }
        section:nth-child(2) {
            white-space: normal;
            word-wrap: break-word;
        }
    }
`;



//Element styles
const SuccessCircleGreen = styled.span`
    padding-top: 4px;
    height: 16px;
    width: 16px;
    background-color: #27c85de0;
    border-radius: 50%;
    display: inline-block;
`;

const ErrorParagraph = styled.p`
    color: #ff4a4a;
    /* background-color: grey; */
    font-weight: bold;
    padding: 10px;
    border-radius: 5px;
`;

const ErrorFilledColored = styled(ErrorFilled)`
    color: #d60000;
`;

const WarningFilledColored = styled(WarningFilled)`
    color: #ff9900;
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
    if (areaName == "Bruker") {
        return <People />
    }
    if (areaName == "Familie") {
        return <Family />
    }
    if (areaName == "EksterneTjenester") {
        return <Service />
    }
    if (areaName == "Lokasjon") {
        return <Globe />
    }
    return <Folder />
}

const handleAndSetStatusIcon = (status: string) => {
    if (status == "OK") {
        return <SuccessCircleGreen ></SuccessCircleGreen>
    }
    if (status == "DOWN") {
        return <ErrorFilledColored />
    }
    if (status == "ISSUE") {
        return <WarningFilledColored />
    }
    return status
}

const DigitalServicesLandingPage = () => {
    const [areas, setAreas] = useState([])
    const [isLoading, setIsLoading] = useState(false)

    useEffect(() => {
        (async function () {
            setIsLoading(true)
            const newAreas = await fetchData()
            // const newAreas = await AsyncFetchNavDigitalServices
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
            {areas.map(area => {
                return (
                    <PortalServiceTile key={area.name} area={area}/>
                )
            })}
        </DigitalServicesContainer>
    )

        // <DigitalServicesContainer>
        //     {areas.map(area => (
        //         <PanelCustomized  key={area.name}>
        //             <div>
        //                 <UndertittelCustomized>
        //                     {handleAndSetNavIcon(area.name)}
        //                     {area.name}
        //                 </UndertittelCustomized>
        //                 <ServicesList>
        //                     {area.services.map(service => (
        //                         <li key={service.name}>
        //                             <section> {handleAndSetStatusIcon(service.status)}</section><section>{service.name}</section>
        //                         </li>
        //                     ))}
        //                 </ServicesList>

        //             </div>
        //         </PanelCustomized>
        //     ))}
        // </DigitalServicesContainer>


}

export default DigitalServicesLandingPage