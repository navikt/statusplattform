import styled from 'styled-components'

import Lenke from 'nav-frontend-lenker';
import { Normaltekst, } from "nav-frontend-typografi";

const Nav = styled.div `
    height: 2.75rem;
	width: 100%;
    background-color: white;
    /* border-bottom: #c6c2bf 1px solid; */
	display: flex;
	align-items: center;
	> ul {
		padding-left: 75px;
	}
	> ul > li {
		display: inline-block;
		outline: none;
	}
	a {
		height: calc(100% + 1px);
		display: inline-block;
		text-decoration: none;
		color: black;
		&active {
			border-bottom: #0067c5 3px solid;
		}
	}
`

const LenkeCustomized = styled(Lenke)`
	border-bottom: transparent 3px solid;
	:hover {
		border-bottom: #0067c5 2px solid;
	}
	:active {
		border-bottom: #0067c5 3px solid;
	}
`;

const LinkWrapper = styled.div`
	height: 100%;
    margin: 0 1rem;
    border-bottom: transparent 3px solid;
	display: flex;
    align-items: center;
	justify-content: center;
`;

const NormalTekstCustomized = styled(Normaltekst)`
	font-size: 1rem;
	line-height: 1.375rem;
`;


export default function Navbar() {
	return (
		<Nav>
			<ul role="tablist">
				<li role="tab">
					<LinkWrapper>
						<LenkeCustomized href="/">

							<NormalTekstCustomized>Privatperson</NormalTekstCustomized>
						</LenkeCustomized>
					</LinkWrapper>
				</li>
				<li role="tab">
					<LinkWrapper>
						<LenkeCustomized href="/arbeidsgiver">
							<NormalTekstCustomized>Arbeidsgiver</NormalTekstCustomized>
						</LenkeCustomized>
					</LinkWrapper>
				</li>
			</ul>
		</Nav>
	)
}