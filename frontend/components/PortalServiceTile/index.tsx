import styled from 'styled-components'
import { useState } from "react";


import { Bag, Folder, PensionBag, HealthCase, ErrorFilled, WarningFilled, Employer, Information, People, Family, Service, Globe } from '@navikt/ds-icons'
import Panel from 'nav-frontend-paneler';
import { Undertittel } from "nav-frontend-typografi";


const PanelCustomized = styled(Panel)`
    color: var(--navBla);
    background-color: var(--navGraBakgrunn);
  

    h2 svg:first-child {
        display: none;
    }
    > div {
        h2 svg:first-child {
            width: 1.778rem;
            height: 1.778rem;
        }
        h2 {
            word-break: break-all;
            font-size: 1.25rem;
        }
    }
    @media (min-width: 468px){
        h2 svg:first-child {
            display: block;
        }
    }
    :hover {
        p {
            text-decoration: underline;
        }
        cursor: pointer;
    }
    ${({ expanded }) => expanded && `
       
        
    `}

`;

const UndertittelCustomized = styled(Undertittel)`
    display: flex;
    align-items: center;
    flex-direction: row;
    border-radius: 10px;
    background-color:white;
    padding: 1.4rem;
    svg {
        margin-right: 10px;
    }
`;

const ServicesList = styled.ul`
    padding: 10%;
    margin-top: -10%;
    margin-left:0;
    border-radius:0 0 10px 10px;
    color:black;
    background-color:white;
    > li {
        display: flex;
        justify-content: flex-start;
        list-style-type: none;
        section {
            display: flex;
            align-items: center;
        }
        section:nth-child(2) {
            white-space: normal;
            word-wrap: break-word;
        }
    }
    @media (min-width: 250px){
        > li {
            margin: 5px 0px 5px 0px;
        }
    }
`;

//Element styles
const SuccessCircleGreen = styled.span`
    margin-right: 10px;
    height: 16px;
    width: 16px;
    background-color: var(--navGronn);
    border-radius: 50%;
    display: inline-block;
`;

const WarningCircleOrange = styled.span`
    margin-right: 10px;
    height: 16px;
    width: 16px;
    background-color: var(--navOransje);
    border-radius: 50%;
    display: inline-block;
`;

const ErrorCircleRed = styled.span`
    margin-right: 10px;
    height: 16px;
    width: 16px;
    background-color: var(--redError);
    border-radius: 50%;
    display: inline-block;
`;

// Remove if decided not to use nav-icons with exclamation-mark ++
const ErrorFilledColored = styled(ErrorFilled)`
    color: var(--redError);
`;
// Remove if decided not to use nav-icons with exclamation-mark ++
const WarningFilledColored = styled(WarningFilled)`
    color: var(--navOransje);
`;

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
        return <SuccessCircleGreen />
    }
    if (status == "DOWN") {
        // return <ErrorFilledColored />
        return <ErrorCircleRed/>
    }
    if (status == "ISSUE") {
        // return <WarningFilledColored />
        return <WarningCircleOrange />
    }
    return status
}

export interface PortalServiceTileProps {
    area: any;
    expanded:boolean;
}



export const PortalServiceTile = ({area }: PortalServiceTileProps) => {
    const [expanded, setExpanded] = useState(false)
    const handleExpand = () => {
        setExpanded(!expanded);
   
    }
    return (
        <PanelCustomized expanded={expanded} onClick={() => handleExpand()}>
            <div>
                
                <UndertittelCustomized>
                    <section>{ handleAndSetStatusIcon(area.status)}</section>
                    <section> {handleAndSetNavIcon(area.name)}</section>
                    <section>{area.name}</section>
                </UndertittelCustomized> 
                {expanded &&
                <ServicesList>
                    {area.services.map(service => (
                        <li key={service.name}>
                            <section> {handleAndSetStatusIcon(service.status)}</section><section>{service.name}</section>
                        </li>
                    ))}
                </ServicesList>
                }

            </div>
        </PanelCustomized>
    )
}

// export default PortalServiceTile