import styled from 'styled-components'

import { Bag, Folder, PensionBag, HealthCase, ErrorFilled, WarningFilled, Employer, Information, People, Family, Service, Globe } from '@navikt/ds-icons'
import Panel from 'nav-frontend-paneler';
import { Undertittel } from "nav-frontend-typografi";


const PanelCustomized = styled(Panel)`
    color: #0067C5;
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
    @media (min-width: 250px){
        > li {
            margin: 5px;
        }
    }
`;

//Element styles
const SuccessCircleGreen = styled.span`
    padding-top: 4px;
    height: 16px;
    width: 16px;
    background-color: #06893A;
    border-radius: 50%;
    display: inline-block;
`;

const WarningCircleOrange = styled.span`
    padding-top: 4px;
    height: 16px;
    width: 16px;
    background-color: #FF9100;
    border-radius: 50%;
    display: inline-block;
`;

const ErrorCircleRed = styled.span`
    padding-top: 4px;
    height: 16px;
    width: 16px;
    background-color: #BA3A26;
    border-radius: 50%;
    display: inline-block;
`;

// Remove if decided not to use nav-icons with exclamation-mark ++
const ErrorFilledColored = styled(ErrorFilled)`
    color: #BA3A26;
`;
// Remove if decided not to use nav-icons with exclamation-mark ++
const WarningFilledColored = styled(WarningFilled)`
    color: #FF9100;
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
        return <SuccessCircleGreen ></SuccessCircleGreen>
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
}

export function PortalServiceTile({area}: PortalServiceTileProps) {

    return (
        <PanelCustomized>
            <div>
                <UndertittelCustomized>
                    {handleAndSetNavIcon(area.name)}
                    {area.name}
                </UndertittelCustomized>
                <ServicesList>
                    {area.services.map(service => (
                        <li key={service.name}>
                            <section> {handleAndSetStatusIcon(service.status)}</section><section>{service.name}</section>
                        </li>
                    ))}
                </ServicesList>

            </div>
        </PanelCustomized>
    )
}

// export default PortalServiceTile